package com.djnd.cinema_java_spring.config;

import java.time.Duration;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.hibernate.cache.jcache.ConfigSettings;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.djnd.cinema_java_spring.domain.entity.Role;
import com.djnd.cinema_java_spring.domain.entity.User;
import com.djnd.cinema_java_spring.repository.UserRepository;

/**
 * set time to live and quantity of record cache ram
 * tạo các ngăn để để app cache level 2
 */
@Configuration
@EnableCaching
public class CacheConfiguration {
    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(@Value("${djnd.cache.ehcache.max-entries}") long maxEntries,
            @Value("${djnd.cache.ehcache.time-to-live-seconds}") long tts) {
        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class,
                        Object.class,
                        ResourcePoolsBuilder.heap(maxEntries) // convert properties
                )
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(tts))) // to properties
                        .build());
    }

    /**
     * Tạo CacheManager (JCache) thủ công vì spring.cache.type=redis sẽ vô hiệu hóa
     * auto-configuration của JCache.
     */
    @Bean
    public javax.cache.CacheManager ehcacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        javax.cache.CacheManager cm = provider.getCacheManager();

        createCache(cm, UserRepository.USERS_BY_LOGIN_CACHE);
        createCache(cm, UserRepository.USERS_BY_EMAIL_CACHE);
        createCache(cm, User.class.getName());
        createCache(cm, User.class.getName() + ".role");
        createCache(cm, Role.class.getName() + ".permissions");

        return cm;
    }

    /**
     * kích hoạt cache level 2
     * 
     * @param cacheManager
     * @return
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
