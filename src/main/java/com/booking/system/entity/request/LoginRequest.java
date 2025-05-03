package com.booking.system.entity.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
