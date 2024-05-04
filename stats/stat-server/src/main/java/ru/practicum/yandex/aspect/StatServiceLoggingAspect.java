package ru.practicum.yandex.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class StatServiceLoggingAspect {


    @Pointcut("execution(* ru.practicum.yandex.service.*.*(..))")
    public void serviceMethods() {}

    @After("serviceMethods()")
    public void logServiceMethodCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("Method {} in class {} was called.", methodName, className);
    }
}
