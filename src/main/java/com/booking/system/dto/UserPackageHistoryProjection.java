package com.booking.system.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface UserPackageHistoryProjection {
    Long getOwnPackageId();
    Integer getRemainingCredits();
    String getName();
    String getPackageName();
    Double getPrice();
    Integer getCreditAmount();
    Integer getExpirationDays();
    LocalDateTime getCreatedOn();
    LocalDateTime getUpdatedOn();
}