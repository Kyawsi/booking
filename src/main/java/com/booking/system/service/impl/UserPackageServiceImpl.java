package com.booking.system.service.impl;

import com.booking.system.dto.UserPackageHistoryProjection;
import com.booking.system.dto.UserPackageHistoryResponse;
import com.booking.system.entity.model.OAuthUser;
import com.booking.system.entity.model.PackageModule;
import com.booking.system.entity.model.UserPackage;
import com.booking.system.entity.request.UserPackageRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.PackageModuleRepository;
import com.booking.system.repository.UserPackageRepository;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.UserPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserPackageServiceImpl implements UserPackageService {
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final PackageModuleRepository packageModuleRepository;


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
    public ResponseFormat findPurchasedPackageHistory(String username) {
        ResponseFormat responseFormat = null;

        try {
            OAuthUser user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new SystemException("User not found"));

            UserPackageHistoryProjection projection = userPackageRepository
                    .findPurchasedPackageHistoryNative(user.getId())
                    .orElseThrow(() -> new SystemException("Purchased history data not found"));

            ZonedDateTime createdOn = projection.getCreatedOn().atZone(ZoneId.systemDefault());
            ZonedDateTime updatedOn = projection.getUpdatedOn().atZone(ZoneId.systemDefault());
            ZonedDateTime expiryDate = createdOn.plusDays(projection.getExpirationDays());
            String status = ZonedDateTime.now().isAfter(expiryDate) ? "EXPIRED" : "AVAILABLE";

            UserPackageHistoryResponse response = UserPackageHistoryResponse.builder()
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
