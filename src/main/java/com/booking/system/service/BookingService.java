package com.booking.system.service;

import com.booking.system.entity.request.BookingCancelRequest;
import com.booking.system.entity.request.BookingRequest;
import com.booking.system.entity.response.ResponseFormat;
import org.springframework.transaction.annotation.Transactional;

public interface BookingService {
    @Transactional(rollbackFor = Exception.class)
    ResponseFormat booking(BookingRequest request, String username);

    ResponseFormat cancelBooking(BookingCancelRequest request, String username);
}
