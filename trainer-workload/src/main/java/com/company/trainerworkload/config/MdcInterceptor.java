package com.company.trainerworkload.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class MdcInterceptor implements HandlerInterceptor {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-ID";
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString();
        }
        MDC.put(TRANSACTION_ID_KEY, transactionId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(TRANSACTION_ID_KEY);
    }
}
