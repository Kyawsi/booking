package com.booking.system.service;

import com.booking.system.entity.response.ResponseFormat;

public interface CountryService {
    ResponseFormat findCountry(int first, int max);
}
