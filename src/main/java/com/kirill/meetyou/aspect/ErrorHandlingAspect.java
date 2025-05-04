package com.kirill.meetyou.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ErrorHandlingAspect {

    @AfterThrowing(
            pointcut = "execution(* com.kirill.meetyou..*(..))",
            throwing = "ex")
    public void logAfterThrowing(JoinPoint jp, Exception ex) {
        log.error("Exception in {}.{}() with cause = {}",
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName(),
                ex.getCause() != null ? ex.getCause() : "NULL");
    }
}