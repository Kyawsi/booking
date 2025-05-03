package com.booking.system.service;

import com.booking.system.entity.request.UserPackageRequest;
import com.booking.system.entity.response.ResponseFormat;
import org.springframework.transaction.annotation.Transactional;

public interface UserPackageService {
    @Transactional(rollbackFor = Exception.class)
    ResponseFormat purchasedPackage(UserPackageRequest request, String username);

    ResponseFormat findPurchasedPackageHistory(String username, int first, int max);
}
