package com.booking.system.service;

import com.booking.system.entity.request.ClassScheduleRequest;
import com.booking.system.entity.response.ResponseFormat;
import org.springframework.transaction.annotation.Transactional;

public interface ClassScheduleService {
    ResponseFormat getClassSchedule(String username,int first, int max);

    @Transactional(rollbackFor = Exception.class)
    ResponseFormat create(ClassScheduleRequest request);
}
