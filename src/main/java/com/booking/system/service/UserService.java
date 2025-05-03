package com.booking.system.service;

import com.booking.system.entity.request.PersonCreateUpdateRequest;
import com.booking.system.entity.response.ResponseFormat;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    @Transactional(rollbackFor = Exception.class)
    ResponseFormat createUser(PersonCreateUpdateRequest request);

    ResponseFormat brief(String email);
}
