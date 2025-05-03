package com.booking.system.entity.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    private String name;
    private String countryGuid;
    private Double price;
    private Integer creditAmount;
    private Integer expirationDays;

}
