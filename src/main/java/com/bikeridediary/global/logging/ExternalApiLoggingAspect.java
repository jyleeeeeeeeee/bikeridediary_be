package com.bikeridediary.global.logging;

import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.domain.apicalllog.service.ApiCallLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@Aspect
@Order(10)  // 트랜잭션 AOP(Integer.MAX_VALUE)보다 먼저 실행
@RequiredArgsConstructor
public class ExternalApiLoggingAspect {
    private final ApiCallLogService apiCallLogService;

    public Object logApiCall(ProceedingJoinPoint joinPoint, LogExternalApi logExternalApi) throws Throwable {
        long start = System.currentTimeMillis();
        UUID userId = getUserId();

        String endpoint = extractEndpoint(joinPoint);

        Throwable caughtException = null;
        Integer statusCode = null;

        try {
            Object result = joinPoint.proceed();
            statusCode = 200;
            return result;
        } catch (Throwable ex) {
            caughtException = ex;
            throw ex;
        } finally {
            long responseTimeMs = System.currentTimeMillis() - start;

            Map<String, Object> params = null;
            if (logExternalApi.logParams()) {
                params = extractParams(joinPoint);
            }

            String errorMessage = caughtException != null
                    ? caughtException.getClass().getSimpleName() + ": " + caughtException.getMessage()
                    : null;

            // 1. RequestContextHolder에서 현재 HttpServletRequest 추출
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            // HTTP Method 추출 ("GET", "POST", "PUT", "DELETE" 등)
            String httpMethod = request.getMethod();


            apiCallLogService.saveLog(
                    userId, logExternalApi.apiName(),
                    endpoint, httpMethod,
                    statusCode, (int) responseTimeMs,
                    params, errorMessage
            );
        }

    }

    private UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();
        if(principal instanceof CustomUserDetails userDetails) return userDetails.getUserId();

        return null;
    }

    private String extractEndpoint(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof String str && str.startsWith("http")) {
                        int queryStart = str.indexOf('?');
                        return queryStart >= 0 ? str.substring(0, queryStart) : str;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[ExternalApiLogging] endpoint 추출 실패 (무시): {}", e.getMessage());
        }
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        return sig.getDeclaringType().getSimpleName() + "." + sig.getName();
    }

    private Map<String, Object> extractParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature sig = (MethodSignature) joinPoint.getSignature();
            Parameter[] parameters = sig.getMethod().getParameters();
            Object[] args = joinPoint.getArgs();

            Map<String, Object> raw = new LinkedHashMap<>();
            for (int i = 0; i < parameters.length; i++) {
                Object value = (args != null && i < args.length) ? args[i] : null;
                raw.put(parameters[i].getName(), value != null ? value.toString() : null);
            }
            return SensitiveParamsFilter.filter(raw);
        } catch (Exception e) {
            log.debug("[ExternalApiLogging] params 추출 실패 (무시): {}", e.getMessage());
            return Map.of();
        }
    }
}
