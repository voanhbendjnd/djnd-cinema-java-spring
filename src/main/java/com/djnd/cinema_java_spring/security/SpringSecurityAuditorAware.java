package com.djnd.cinema_java_spring.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.djnd.cinema_java_spring.config.constants.Constants;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    /**
     * field who effect to entity for @modified
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(SecurityUtils.getCurrentUserLogin().orElse(Constants.SYSTEM));
    }

}
