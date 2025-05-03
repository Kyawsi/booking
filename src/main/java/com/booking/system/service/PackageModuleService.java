package com.booking.system.service;

import com.booking.system.entity.response.ResponseFormat;

public interface PackageModuleService {
    ResponseFormat getAvailablePackagesForUser(String username);
}
