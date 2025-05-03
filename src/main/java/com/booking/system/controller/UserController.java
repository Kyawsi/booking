package com.booking.system.controller;

import com.booking.system.entity.request.LoginRequest;
import com.booking.system.entity.request.PersonCreateUpdateRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.service.UserService;
import com.booking.system.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping(value = "/create")
    public ResponseEntity<ResponseFormat> create(@RequestBody PersonCreateUpdateRequest request) {
        ResponseFormat response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ResponseFormat> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenUtil.generateToken(authentication.getName());

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);

        ResponseFormat response = ResponseFormat.createSuccessResponse(tokenMap, "Login successful");
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/brief")
    public ResponseEntity<ResponseFormat> brief(Principal principal) {
        ResponseFormat response = userService.brief(principal.getName());
        return ResponseEntity.ok(response);
    }


}
