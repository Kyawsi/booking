package com.booking.system.controller;

import com.booking.system.entity.request.UserPackageRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.service.UserPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/v1/user/package")
@RequiredArgsConstructor
@Slf4j
public class UserPackageController {
    private final UserPackageService userPackageService;

    @PostMapping(value = "/purchase")
    public ResponseEntity<ResponseFormat> create(@RequestBody UserPackageRequest request,
                                                 Principal principal) {
        ResponseFormat response = userPackageService.purchasedPackage(request,principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/history")
    public ResponseEntity<ResponseFormat> findPurchasedPackageHistory(

                                                 Principal principal) {
        ResponseFormat response = userPackageService.findPurchasedPackageHistory(principal.getName());
        return ResponseEntity.ok(response);
    }


}
