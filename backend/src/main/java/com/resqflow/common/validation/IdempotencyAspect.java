package com.resqflow.common.validation;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class IdempotencyAspect {
    private static final Logger logger = LoggerFactory.getLogger(IdempotencyAspect.class);

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${resqflow.idempotency.ttl-seconds:3600}")
    private long ttlSeconds;

    public IdempotencyAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(com.resqflow.common.validation.Idempotent)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // If the header is missing, proceed normally without caching
            return joinPoint.proceed();
        }

        String cacheKey = "idempotency:" + idempotencyKey;
        Object cachedResponse = redisTemplate.opsForValue().get(cacheKey);

        if (cachedResponse != null) {
            logger.info("Idempotent request detected. Returning cached response for key: {}", idempotencyKey);
            return cachedResponse;
        }

        // Execute target method
        Object result = joinPoint.proceed();

        // Cache the result
        if (result != null) {
            redisTemplate.opsForValue().set(cacheKey, result, ttlSeconds, TimeUnit.SECONDS);
            logger.debug("Cached response for idempotency key: {}", idempotencyKey);
        }

        return result;
    }
}
