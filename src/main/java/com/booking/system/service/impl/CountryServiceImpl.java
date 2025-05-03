package com.booking.system.service.impl;

import com.booking.system.dto.CountryResponse;
import com.booking.system.entity.response.ListResponse;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.CountryRepository;
import com.booking.system.service.CountryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CountryServiceImpl implements CountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public ResponseFormat findCountry(int first, int max) {
        ResponseFormat responseFormat = null;
        ListResponse response =ListResponse.builder()
                .items(new ArrayList<>())
                .totalRecords(0)
                .build();
        try {
            Pageable pageable = PageRequest.of(first, max);

            long total = countryRepository.countCountry();
            List<CountryResponse> country = countryRepository
                    .findCountry(pageable);

            response.setItems(country);
            response.setTotalRecords(total);

            responseFormat = new ResponseFormat();
            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Country List fetched successfully"));
            responseFormat.setData(Optional.of(response));

        } catch (Exception e) {
            log.error("Error occurred while fetching Country List: {}", e.getMessage(), e);
            throw new SystemException(e);
        }

        return responseFormat;
    }
}
