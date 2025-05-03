package com.booking.system.service;

import com.booking.system.entity.request.PackageRequest;
import com.booking.system.entity.response.ResponseFormat;
import org.springframework.transaction.annotation.Transactional;

public interface PackageModuleService {
    ResponseFormat getAvailablePackagesForUser(String username,int first, int max);

    @Transactional(rollbackFor = Exception.class)
    ResponseFormat create(PackageRequest request);
}
