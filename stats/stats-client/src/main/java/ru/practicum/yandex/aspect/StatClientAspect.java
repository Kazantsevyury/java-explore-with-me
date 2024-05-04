package ru.practicum.yandex.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class StatClientAspect {

    @Before("execution(* ru.practicum.yandex.StatClient.*(..))")
    public void logBeforeMethodCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("Перед вызовом метода: {}", methodName);
    }

    @AfterReturning(
            pointcut = "execution(* ru.practicum.yandex.StatClient.*(..))",
            returning = "result"
    )
    public void logAfterMethodCall(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("После вызова метода: {}. Результат: {}", methodName, result);
    }
}
