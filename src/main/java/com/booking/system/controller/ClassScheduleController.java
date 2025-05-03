package com.booking.system.controller;

import com.booking.system.entity.request.ClassScheduleRequest;
import com.booking.system.entity.request.PackageRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/class")
@RequiredArgsConstructor
@Slf4j
public class ClassScheduleController {
    private final ClassScheduleService classScheduleService;
    @GetMapping(value = "/list")
    public ResponseEntity<ResponseFormat> getClassSchedule(
            Principal principal,
            @RequestParam(value = "first", required = false, defaultValue="0") int first,
            @RequestParam(value = "max", required = false, defaultValue= "10000" ) int max) {
        ResponseFormat response = classScheduleService.getClassSchedule(principal.getName(),first,max);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<ResponseFormat> create(@RequestBody ClassScheduleRequest request) {
        ResponseFormat response = classScheduleService.create(request);
        return ResponseEntity.ok(response);
    }
}
