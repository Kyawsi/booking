package com.booking.system.service;

import com.booking.system.entity.response.ResponseFormat;

public interface ClassScheduleService {
    ResponseFormat getClassSchedule(String username,int first, int max);
}
