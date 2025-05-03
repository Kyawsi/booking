package com.booking.system.service.impl;

import com.booking.system.entity.model.*;
import com.booking.system.entity.request.BookingRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.BookingRepository;
import com.booking.system.repository.ClassScheduleRepository;
import com.booking.system.repository.UserPackageRepository;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final UserPackageRepository userPackageRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final BookingRepository bookingRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseFormat booking(BookingRequest request, String username) {
        ResponseFormat responseFormat = null;

        try {
            OAuthUser user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new SystemException("User not found"));

            UserPackage userPackage = userPackageRepository.findById(request.getUserPackageId())
                    .orElseThrow(() -> new SystemException("User Package not found"));

            ClassSchedule classSchedule = classScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new SystemException("Class not found"));

            Integer requiredCredits = classSchedule.getRequiredCredits();
            Integer remainingCredits = userPackage.getRemainingCredits();

            if (requiredCredits == null || requiredCredits <= 0) {
                throw new SystemException("Invalid required credit configuration for class");
            }

            if (remainingCredits == null || remainingCredits < requiredCredits) {
                throw new SystemException("Not enough credits to book this class");
            }

            userPackage.setRemainingCredits(remainingCredits - requiredCredits);
            userPackage.setUpdatedOn(ZonedDateTime.now());
            userPackageRepository.save(userPackage);

            String status="booked";
            Booking booking = Booking.builder()
                    .user(user)
                    .userPackage(userPackage)
                    .schedule(classSchedule)
                    .status(status)
                    .createdOn(ZonedDateTime.now())
                    .updatedOn(ZonedDateTime.now())
                    .build();
            bookingRepository.save(booking);

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Class booked successfully"));
            responseFormat.setData(Optional.of(Map.of("userId", user.getId(), "bookingId", booking.getId())));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error booking class: {}", ExceptionUtils.getStackTrace(e));
            throw new SystemException(e);
        }

        return responseFormat;
    }

}
