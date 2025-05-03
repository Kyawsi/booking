package com.booking.system.service.impl;

import com.booking.system.dto.LoginProfileResponse;
import com.booking.system.entity.model.Country;
import com.booking.system.entity.model.OAuthUser;
import com.booking.system.entity.request.PersonCreateUpdateRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.CountryRepository;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseFormat createUser(PersonCreateUpdateRequest request) {
        ResponseFormat responseFormat = null;

        try {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }

            Country country = countryRepository.findByGuid(request.getCountryGuid())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid country ID"));

            boolean isVerified = !(Boolean.TRUE.equals(request.getSelfRegister()));

            OAuthUser user = OAuthUser.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getUsername().replace("/", "-"))
                    .country(country)
                    .isVerified(isVerified)
                    .createdOn(ZonedDateTime.now())
                    .updatedOn(ZonedDateTime.now())
                    .build();

            userRepository.save(user);

            if (!isVerified) {
                boolean emailSent = sendMockEmailVerification(user.getEmail());
                if (!emailSent) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send verification email");
                }
            }

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("User account created successfully"));
            responseFormat.setData(Optional.of(Map.of("userId", user.getId())));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user: {}", ExceptionUtils.getStackTrace(e));
            throw new SystemException(e);
        }
        return responseFormat;
    }

    private boolean sendMockEmailVerification(String email) {
        return new Random().nextBoolean();
    }

    @Override
    public ResponseFormat brief(String username) {
        ResponseFormat responseFormat = null;
        try {
            OAuthUser user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new SystemException("User not found"));

            LoginProfileResponse loginResponse = userRepository.findProfileByGuid(user)
                    .orElseThrow(() -> new SystemException("Profile data not found"));

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Fetching person brief successful"));
            responseFormat.setData(Optional.of(loginResponse));

        } catch (Exception e) {
            log.error("Error occurred while fetching user brief info: {}", e.getMessage(), e);
            throw new SystemException(e);
        }
        return responseFormat;
    }



}
