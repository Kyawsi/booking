package com.booking.system.controller;

import com.booking.system.entity.model.OAuthUser;
import com.booking.system.entity.request.ChangePasswordRequest;
import com.booking.system.entity.request.LoginRequest;
import com.booking.system.entity.request.PersonCreateUpdateRequest;
import com.booking.system.entity.response.ResponseFormat;
import com.booking.system.exception.SystemException;
import com.booking.system.repository.UserRepository;
import com.booking.system.service.UserService;
import com.booking.system.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;


@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @PostMapping(value = "/create")
    public ResponseEntity<ResponseFormat> create(@RequestBody PersonCreateUpdateRequest request) {
        ResponseFormat response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ResponseFormat> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
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

    @GetMapping("/verify-email")
    public ResponseEntity<ResponseFormat> verifyMock(@RequestParam Long userId) {
        ResponseFormat responseFormat = new ResponseFormat();
        try {
            OAuthUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

            if (user.getIsVerified()) {
                responseFormat.setSuccess(true);
                responseFormat.setMessage(Optional.of("User already verified"));
                return ResponseEntity.ok(responseFormat);
            }

            user.setIsVerified(true);
            user.setUpdatedOn(ZonedDateTime.now());
            userRepository.save(user);

            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("User verification successful (mock)"));
            responseFormat.setData(Optional.of(Map.of("userId", user.getId())));
            return ResponseEntity.ok(responseFormat);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            responseFormat.setSuccess(false);
            responseFormat.setMessage(Optional.of("Verification failed: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseFormat);
        }
    }


    @PostMapping(value = "/update")
    public ResponseEntity<ResponseFormat> update(
            @RequestBody ChangePasswordRequest request,
            Principal principal) {
        ResponseFormat response = userService.changePassword(request,principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-password-reset")
    public ResponseFormat requestPasswordReset(@RequestParam String email) {
        ResponseFormat responseFormat = new ResponseFormat();

        try {
            OAuthUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

            String otp = generateOtp();

            // Send the OTP to user's email (mocked here)
            boolean emailSent = sendOtpEmail(user.getEmail(), otp);
            if (!emailSent) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email");
            }

            // Save the OTP and timestamp for verification
            user.setPassword(otp);
            user.setUpdatedOn(ZonedDateTime.now());
            userRepository.save(user);

            responseFormat.setSuccess(true);
            responseFormat.setMessage(Optional.of("Password reset OTP sent to your email"));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error requesting password reset: {}", e.getMessage(), e);
            throw new SystemException(e);
        }

        return responseFormat;
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private boolean sendOtpEmail(String email, String otp) {
        String otpMessage = "Your password reset OTP is: " + otp;
        log.info("Sending OTP to {}: {}", email, otpMessage);
        return new Random().nextBoolean();
    }

}
