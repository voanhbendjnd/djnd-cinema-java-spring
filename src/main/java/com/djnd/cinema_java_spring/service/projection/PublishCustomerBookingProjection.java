package com.djnd.cinema_java_spring.service.projection;

import java.math.BigDecimal;
import java.time.Instant;

import com.djnd.cinema_java_spring.domain.enumeration.BookingStatus;

public interface PublishCustomerBookingProjection {
    Long getId();

    String getCreatedBy();

    Instant getCreatedDate();

    String getLastModifiedBy();

    Instant getLastModifiedDate();

    String getBookingCode();

    String getPaymentMethod();

    BookingStatus getStatus();

    BigDecimal getTotalAmount();

    String getCustomerName();

    String getCustomerPhone();

    String getCustomerIdentityCard();

}
