package com.djnd.cinema_java_spring.service.projection;

import java.time.Instant;

public interface ProfileUserProjection extends PublishUserProjection {
    String getIdentityCard();

    String getAddress();

    Integer getLoyaltyPoints();

    Instant getDateOfBirth();
}
