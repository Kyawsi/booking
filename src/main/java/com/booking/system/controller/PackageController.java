package com.booking.system.controller;

import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.service.PackageModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/package")
@RequiredArgsConstructor
@Slf4j
public class PackageController {
    private final PackageModuleService packageModuleService;

    @GetMapping(value = "/list")
    public ResponseEntity<ResponseFormat> getAvailablePackagesForUser(Principal principal) {
        ResponseFormat response = packageModuleService.getAvailablePackagesForUser(principal.getName());
        return ResponseEntity.ok(response);
    }

}
