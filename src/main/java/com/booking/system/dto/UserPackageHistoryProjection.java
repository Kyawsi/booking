package com.booking.system.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface UserPackageHistoryProjection {
    Integer getRemainingCredits();
    String getName();
    String getPackageName();
    Double getPrice();
    Integer getCreditAmount();
    Integer getExpirationDays();
    LocalDateTime getCreatedOn();
    LocalDateTime getUpdatedOn();
}