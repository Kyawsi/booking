package com.booking.system.service.impl;

import com.booking.system.dto.PackageResponse;
import com.booking.system.entity.model.Country;
import com.booking.system.entity.model.User;
import com.booking.system.entity.model.PackageModule;
import com.booking.system.entity.request.PackageRequest;
import com.booking.system.entity.response.ListResponse;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.CountryRepository;
import com.booking.system.repository.PackageModuleRepository;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.PackageModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageModuleServiceImpl implements PackageModuleService {

    private final UserRepository userRepository;
    private final PackageModuleRepository packageModuleRepository;
    private final CountryRepository countryRepository;

    @Override
    public ResponseFormat getAvailablePackagesForUser(String username,int first, int max) {
        ResponseFormat responseFormat = null;
        ListResponse response =ListResponse.builder()
                .items(new ArrayList<>())
                .totalRecords(0)
                .build();
        try {
            Pageable pageable= PageRequest.of(first,max);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new SystemException("User not found"));

            Country country = countryRepository.findById(user.getCountry().getId())
                    .orElseThrow(() -> new SystemException("Country not found"));

            long total = packageModuleRepository.countPackageByCountry(country);
            List<PackageResponse> packageResponseList = packageModuleRepository
                    .findPackageByCountry(country,pageable);

            response.setItems(packageResponseList);
            response.setTotalRecords(total);
            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Fetching Package List Related By Country Successful"));
            responseFormat.setData(Optional.of(response));

        } catch (Exception e) {
            log.error("Error occurred while fetching package info: {}", e.getMessage(), e);
            throw new SystemException(e);
        }
        return responseFormat;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseFormat create(PackageRequest request) {
        ResponseFormat responseFormat = null;

        try {
            Country country = countryRepository.findByGuid(request.getCountryGuid())
                    .orElseThrow(() -> new SystemException("Country not found"));

            boolean isNameExistInCountry = packageModuleRepository.existsByNameAndCountry(request.getName(), country);
            if (isNameExistInCountry) {
                throw new SystemException("Package name already exists for the specified country");
            }



            String status = "Available";

            PackageModule packageModule1 = PackageModule.builder()
                    .name(request.getName())
                    .country(country)
                    .price(request.getPrice())
                    .creditAmount(request.getCreditAmount())
                    .expirationDays(request.getExpirationDays())
                    .status(status)
                    .createdOn(ZonedDateTime.now())
                    .updatedOn(ZonedDateTime.now())
                    .build();

            packageModuleRepository.save(packageModule1);

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Create Package Successfully"));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating package: {}", ExceptionUtils.getStackTrace(e));
            throw new SystemException(e);
        }

        return responseFormat;
    }



}
