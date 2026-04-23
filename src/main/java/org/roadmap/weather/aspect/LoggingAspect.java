package org.roadmap.weather.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(org.roadmap.weather.aspect.Loggable)")
    public Object log(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = proceedingJoinPoint.proceed();
            long timeDifference = System.currentTimeMillis() - start;

            if (result instanceof List<?>) {
                int size = ((List<?>) result).size();
                logger.debug("{} found {} items in {}ms", methodName, size, timeDifference);
            } else {
                logger.debug("{} completed in {}ms", methodName, timeDifference);
            }
            return result;
        } catch (Exception ex) {
            long timeDifference = System.currentTimeMillis() - start;
            logger.warn("{} failed in {}ms. {}", methodName, timeDifference, ex.getMessage());
            throw ex;
        }
    }
}
