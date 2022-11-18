package com.project.sharedfolderclient.v1.utils.http.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Wrap @ContextEnabled annotated method with context id
 */
@Aspect
public class ContextWrapper {
    private final Context context;

    @Pointcut("@annotation(contextEnabled)")
    public void callAt(ContextEnabled contextEnabled) {
    }

    @Around("callAt(contextEnabled)")
    public Object around(ProceedingJoinPoint pjp,
                         ContextEnabled contextEnabled) throws Throwable {

            String contextId = UUID.randomUUID().toString();
            context.clear();
            log.debug("Settings context id: {}", kv("contextId",contextId));
            context.setRequestId(contextId);
            return pjp.proceed();

    }
}
