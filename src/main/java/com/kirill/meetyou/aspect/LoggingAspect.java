package com.kirill.meetyou.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.kirill.meetyou.controller..*(..))")
    public void controllerMethods() {}

    @Pointcut("execution(* com.kirill.meetyou.service..*(..))")
    public void serviceMethods() {}

    @Before("controllerMethods() || serviceMethods()")
    public void logMethodCall(JoinPoint jp) {
        log.debug("Method called: {} with args: {}",
                jp.getSignature().toShortString(),
                jp.getArgs());
    }

    @AfterReturning(pointcut = "controllerMethods() || serviceMethods()", returning = "result")
    public void logMethodReturn(JoinPoint jp, Object result) {
        log.debug("Method {} returned: {}",
                jp.getSignature().toShortString(),
                result);
    }
}