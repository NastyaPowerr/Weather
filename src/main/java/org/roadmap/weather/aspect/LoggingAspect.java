package org.roadmap.weather.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Around("@annotation(org.roadmap.weather.aspect.Loggable)")
    public Object log(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = proceedingJoinPoint.proceed();
            long timeDifference = System.currentTimeMillis() - start;

            if (result instanceof List<?>) {
                int size = ((List<?>) result).size();
                log.debug("{} found {} items in {}ms", methodName, size, timeDifference);
            } else {
                log.debug("{} completed in {}ms", methodName, timeDifference);
            }
            return result;
        } catch (Exception ex) {
            long timeDifference = System.currentTimeMillis() - start;
            log.warn("{} failed in {}ms. {}", methodName, timeDifference, ex.getMessage());
            throw ex;
        }
    }
}
