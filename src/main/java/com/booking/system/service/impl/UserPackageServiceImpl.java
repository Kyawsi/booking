package com.booking.system.service.impl;

import com.booking.system.dto.UserPackageHistoryProjection;
import com.booking.system.dto.UserPackageHistoryResponse;
import com.booking.system.entity.model.OAuthUser;
import com.booking.system.entity.model.PackageModule;
import com.booking.system.entity.model.UserPackage;
import com.booking.system.entity.request.UserPackageRequest;
import com.booking.system.entity.response.ListResponse;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.PackageModuleRepository;
import com.booking.system.repository.UserPackageRepository;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.UserPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserPackageServiceImpl implements UserPackageService {

    @Autowired
    private  UserPackageRepository userPackageRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  PackageModuleRepository packageModuleRepository;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseFormat purchasedPackage(UserPackageRequest request, String username) {
        ResponseFormat responseFormat = null;

        try {

            OAuthUser user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new SystemException("User not found"));

            PackageModule packageModule = packageModuleRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new SystemException("Package not found"));

            Optional<UserPackage> existingUserPackageOpt =
                    userPackageRepository.findByUserAndPackageModule(user, packageModule);

            UserPackage userPackage;

            if (existingUserPackageOpt.isPresent()) {
                userPackage = existingUserPackageOpt.get();
                int updatedCredits = userPackage.getRemainingCredits() + packageModule.getCreditAmount();
                userPackage.setRemainingCredits(updatedCredits);
                userPackage.setUpdatedOn(ZonedDateTime.now());
                userPackage.setCreatedOn(ZonedDateTime.now());
            } else {
                userPackage = UserPackage.builder()
                        .user(user)
                        .packageModule(packageModule)
                        .remainingCredits(packageModule.getCreditAmount())
                        .createdOn(ZonedDateTime.now())
                        .updatedOn(ZonedDateTime.now())
                        .build();
            }

            userPackageRepository.save(userPackage);

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("User Purchased Package Successfully"));
            responseFormat.setData(Optional.of(Map.of("userId", user.getId())));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error purchased package: {}", ExceptionUtils.getStackTrace(e));
            throw new SystemException(e);
        }
        return responseFormat;
    }

    @Override
    public ResponseFormat findPurchasedPackageHistory(String username, int first, int max) {
        ResponseFormat responseFormat = null;
        ListResponse response =ListResponse.builder()
                .items(new ArrayList<>())
                .totalRecords(0)
                .build();
        try {
            Pageable pageable = PageRequest.of(first, max);

            OAuthUser user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new SystemException("User not found"));

            long total= userPackageRepository.countPurchasedPackageHistory(user.getId());
            List<UserPackageHistoryProjection> projections = userPackageRepository
                    .findPurchasedPackageHistoryNative(user.getId(), pageable);


            List<UserPackageHistoryResponse> responseList = projections.stream()
                    .map(projection -> {
                        ZonedDateTime createdOn = projection.getCreatedOn().atZone(ZoneId.systemDefault());
                        ZonedDateTime updatedOn = projection.getUpdatedOn().atZone(ZoneId.systemDefault());
                        ZonedDateTime expiryDate = createdOn.plusDays(projection.getExpirationDays());
                        String status = ZonedDateTime.now().isAfter(expiryDate) ? "EXPIRED" : "AVAILABLE";

                        return UserPackageHistoryResponse.builder()
                                .ownPackageId(projection.getOwnPackageId())
                                .name(projection.getName())
                                .packageName(projection.getPackageName())
                                .price(projection.getPrice())
                                .creditAmount(projection.getCreditAmount())
                                .remainingCredits(projection.getRemainingCredits())
                                .expirationDays(projection.getExpirationDays())
                                .createdOn(createdOn)
                                .updatedOn(updatedOn)
                                .status(status)
                                .build();
                    })
                    .collect(Collectors.toList());

            response.setTotalRecords(total);
            response.setItems(responseList);
            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("User purchased package history fetched successfully"));
            responseFormat.setData(Optional.of(response));

        } catch (Exception e) {
            log.error("Error occurred while fetching user purchased package history: {}", e.getMessage(), e);
            throw new SystemException(e);
        }

        return responseFormat;
    }




}
