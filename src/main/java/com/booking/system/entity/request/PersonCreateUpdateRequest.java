package com.booking.system.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonCreateUpdateRequest {

    private String email;

    private String password;

    private String username;

    private String countryGuid;
    private Boolean selfRegister;

}
