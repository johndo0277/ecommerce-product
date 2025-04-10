package com.johndo.product.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
public class RedisConfig {

        @Value("${spring.redis.host}")
        private String redisHost;
        @Value("${spring.redis.port}")
        private int redisPort;

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
                LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisHost, redisPort);
                connectionFactory.afterPropertiesSet();
                return connectionFactory;
        }

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // Configure ObjectMapper for GenericJackson2JsonRedisSerializer
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                objectMapper.activateDefaultTyping(
                                BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                                ObjectMapper.DefaultTyping.NON_FINAL);

                // Use the customized ObjectMapper in GenericJackson2JsonRedisSerializer
                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                // Configure RedisCacheConfiguration
                RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                                .entryTtl(Duration.ofHours(1)); // Set default TTL for caches: 1 hour

                // Build and return RedisCacheManager
                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(cacheConfig)
                                .build();
        }
}

/*
 * use Redis Template config
 * 
 * package com.johndo.product.config;
 * 
 * import org.springframework.context.annotation.Bean;
 * import org.springframework.context.annotation.Configuration;
 * import org.springframework.data.redis.connection.RedisConnectionFactory;
 * import
 * org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
 * import
 * org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
 * import org.springframework.data.redis.serializer.StringRedisSerializer;
 * import org.springframework.data.redis.core.RedisTemplate;
 * 
 * @Configuration
 * public class RedisConfig {
 * 
 * @Bean
 * public RedisConnectionFactory redisConnectionFactory() {
 * // Configures the connection to the Redis server
 * return new LettuceConnectionFactory("redis", 6379);
 * }
 * 
 * @Bean
 * public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory
 * connectionFactory) {
 * RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
 * redisTemplate.setConnectionFactory(connectionFactory);
 * 
 * // Configure the key serializer
 * redisTemplate.setKeySerializer(new StringRedisSerializer());
 * 
 * // Configure the value serializer
 * redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
 * 
 * // Configure serializers for hash keys and values (optional)
 * redisTemplate.setHashKeySerializer(new StringRedisSerializer());
 * redisTemplate.setHashValueSerializer(new
 * GenericJackson2JsonRedisSerializer());
 * 
 * redisTemplate.afterPropertiesSet();
 * return redisTemplate;
 * }
 * }
 */