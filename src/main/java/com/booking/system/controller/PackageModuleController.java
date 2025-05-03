package com.booking.system.controller;

import com.booking.system.entity.request.PackageRequest;
import com.booking.system.entity.request.UserPackageRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.service.PackageModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/package")
@RequiredArgsConstructor
@Slf4j
public class PackageModuleController {
    private final PackageModuleService packageModuleService;

    @GetMapping(value = "/list")
    public ResponseEntity<ResponseFormat> getAvailablePackagesForUser(
            Principal principal,
            @RequestParam(value = "first", required = false, defaultValue="0") int first,
            @RequestParam(value = "max", required = false, defaultValue= "10000" ) int max
                                                                      ) {
        ResponseFormat response = packageModuleService.getAvailablePackagesForUser(
                principal.getName(),first,max);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<ResponseFormat> create(@RequestBody PackageRequest request) {
        ResponseFormat response = packageModuleService.create(request);
        return ResponseEntity.ok(response);
    }
}
