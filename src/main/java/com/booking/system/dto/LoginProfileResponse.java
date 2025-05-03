package com.booking.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginProfileResponse {
    private String email;
    private String name;
    private String countryName;
    private Boolean isVerified;
}
