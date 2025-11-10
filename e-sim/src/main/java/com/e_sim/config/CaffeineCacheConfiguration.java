package com.e_sim.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class CaffeineCacheConfiguration {

    public static final String DEFAULT_ROLES_CACHE = "defaultRoles";


    private static final Map<String, CacheConfig> CACHE_CONFIGS = Map.of(
            DEFAULT_ROLES_CACHE, new CacheConfig(2, 10, Duration.ofDays(1))
    );

    @Bean
    public CacheManager cacheManager() {
        CustomCaffeineCacheManager cacheManager = new CustomCaffeineCacheManager();
        cacheManager.registerCaches(CACHE_CONFIGS);
        return cacheManager;
    }


    // Record for cache configuration
    private record CacheConfig(int initialCapacity, int maximumSize, Duration expireAfterWrite) {}

    // Custom cache manager with individual cache configurations
    private static class CustomCaffeineCacheManager extends CaffeineCacheManager {
        public void registerCaches(Map<String, CacheConfig> configs) {
            configs.forEach((name, config) -> {
                Caffeine<Object, Object> builder = Caffeine.newBuilder()
                        .initialCapacity(config.initialCapacity())
                        .maximumSize(config.maximumSize())
                        .expireAfterWrite(config.expireAfterWrite())
                        .recordStats()

                        .evictionListener((key, value, cause) ->
                                log.info("Evicted {} from cache '{}' due to {}", key, name, cause))
                        .removalListener((key, value, cause) ->
                                log.info("Removed {} from cache '{}' due to {}", key, name, cause));

                super.registerCustomCache(name, builder.build());
                log.info("Configured cache '{}' with: initialCapacity={}, maximumSize={}, expireAfterWrite={}",
                        name, config.initialCapacity(), config.maximumSize(), config.expireAfterWrite());
            });
        }
    }
}
