package com.booking.system.service.impl;

import com.booking.system.dto.PackageResponse;
import com.booking.system.entity.model.Country;
import com.booking.system.entity.model.OAuthUser;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.CountryRepository;
import com.booking.system.repository.PackageModuleRepository;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.PackageModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageModuleServiceImpl implements PackageModuleService {

    private final UserRepository userRepository;
    private final PackageModuleRepository packageModuleRepository;
    private final CountryRepository countryRepository;

    @Override
    public ResponseFormat getAvailablePackagesForUser(String username) {
        ResponseFormat responseFormat = null;
        try {
            OAuthUser user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new SystemException("User not found"));

            Country country = countryRepository.findById(user.getCountry().getId())
                    .orElseThrow(() -> new SystemException("Country not found"));

            PackageResponse packageResponse = packageModuleRepository.findPackageByCountry(country)
                    .orElseThrow(() -> new SystemException("Package data not found"));

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Fetching Package List Related By Country Successful"));
            responseFormat.setData(Optional.of(packageResponse));

        } catch (Exception e) {
            log.error("Error occurred while fetching package info: {}", e.getMessage(), e);
            throw new SystemException(e);
        }
        return responseFormat;
    }


}
