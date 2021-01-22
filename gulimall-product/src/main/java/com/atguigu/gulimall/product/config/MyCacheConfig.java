package com.atguigu.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


@EnableCaching
@Configuration
public class MyCacheConfig {


    @Bean
    RedisCacheConfiguration RedisCacheConfiguration(CacheProperties cacheProperties){
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if(redisProperties.getTimeToLive()!=null)
            configuration = configuration.entryTtl(redisProperties.getTimeToLive());
        if(redisProperties.getKeyPrefix()!=null)
            configuration = configuration.prefixKeysWith(redisProperties.getKeyPrefix());
        if(!redisProperties.isCacheNullValues())
            configuration = configuration.disableCachingNullValues();
        if(!redisProperties.isUseKeyPrefix())
            configuration = configuration.disableKeyPrefix();
        return configuration;
    }
}
