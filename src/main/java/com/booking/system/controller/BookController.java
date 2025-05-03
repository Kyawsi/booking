package com.booking.system.controller;


import com.booking.system.entity.request.BookingRequest;
import com.booking.system.entity.request.UserPackageRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/book")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookingService bookingService;
    @PostMapping(value = "/create")
    public ResponseEntity<ResponseFormat> create(@RequestBody BookingRequest request,
                                                 Principal principal) {
        ResponseFormat response = bookingService.booking(request,principal.getName());
        return ResponseEntity.ok(response);
    }
}
