package com.safely.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogAspect {
    // 모든 도메인의 controller 패키지 하위의 모든 메서드를 타겟으로 잡음.
    @Pointcut("execution(* com.safely.domain..controller..*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String params = Arrays.toString(joinPoint.getArgs());

        // 요청 진입 로그
        log.info("[*] Request: [{} {}] -> {}.{}() | Params: {}",
                request.getMethod(), request.getRequestURI(), className, methodName, params);

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed(); // 실제 메서드 실행
            long executionTime = System.currentTimeMillis() - start;

            // 응답 성공 로그
            log.info("[*] Response: {}.{}() | Time: {}ms", className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - start;
            // 예외 발생 로그 (GlobalExceptionHandler로 가기 전 기록)
            log.error("[!] Exception in {}.{}() | Time: {}ms | Message: {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}