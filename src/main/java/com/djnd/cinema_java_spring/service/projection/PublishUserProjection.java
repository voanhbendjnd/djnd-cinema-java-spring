package com.djnd.cinema_java_spring.service.projection;

import java.time.Instant;

import com.djnd.cinema_java_spring.domain.enumeration.UserGender;

public interface PublishUserProjection {
    Long getId();

    String getName();

    String getLogin();

    String getEmail();

    UserGender getGender();

    String getPhone();

    Instant getCreatedDate();

    Instant getLastModifiedDate();

    String getCreatedBy();

    String getLastModifiedBy();
}
