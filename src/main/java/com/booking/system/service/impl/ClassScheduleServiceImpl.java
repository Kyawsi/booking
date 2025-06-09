package com.booking.system.service.impl;

import com.booking.system.dto.ClassScheduleResponse;
import com.booking.system.entity.model.ClassSchedule;
import com.booking.system.entity.model.Country;
import com.booking.system.entity.model.User;
import com.booking.system.entity.request.ClassScheduleRequest;
import com.booking.system.entity.response.ListResponse;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.ClassScheduleRepository;
import com.booking.system.repository.CountryRepository;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
public class ClassScheduleServiceImpl implements ClassScheduleService{
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final ClassScheduleRepository classScheduleRepository;

    @Override
    public ResponseFormat getClassSchedule(String username,int first, int max) {
        ResponseFormat responseFormat = null;
        ListResponse response =ListResponse.builder()
                .items(new ArrayList<>())
                .totalRecords(0)
                .build();
        try {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"User not found"));

            Country country = countryRepository.findById(user.getCountry().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Country not found"));

            Pageable pageable= PageRequest.of(first,max);


            List<ClassScheduleResponse> classScheduleList = classScheduleRepository.findClassScheduleByCountry(country, pageable);
            long total=classScheduleRepository.countClassScheduleByCountry(country);

            response.setItems(classScheduleList);
            response.setTotalRecords(total);
            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Fetching Class Schedule List Related By Country Successful"));
            responseFormat.setData(Optional.of(response));

        } catch (Exception e) {
            log.error("Error occurred while fetching Class Schedule List info: {}", e.getMessage(), e);
            throw new SystemException(e);
        }
        return responseFormat;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseFormat create(ClassScheduleRequest request) {
        ResponseFormat responseFormat = null;

        try {

            Country country = countryRepository.findByGuid(request.getCountryGuid())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Country not found"));

            ClassSchedule classSchedule = ClassSchedule.builder()
                    .country(country)
                    .title(request.getTitle())
                    .endTime(request.getEndTime())
                    .startTime(request.getStartTime())
                    .requiredCredits(request.getRequiredCredits())
                    .slotCount(request.getSlotCount())
                    .createdOn(ZonedDateTime.now())
                    .updatedOn(ZonedDateTime.now())
                    .build();

            classScheduleRepository.save(classSchedule);

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Create Class Successfully"));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error purchased Class: {}", ExceptionUtils.getStackTrace(e));
            throw new SystemException(e);
        }

        return responseFormat;
    }
}
