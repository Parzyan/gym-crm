package com.company.gym.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        filterChain.doFilter(requestWrapper, responseWrapper);

        long timeTaken = System.currentTimeMillis() - startTime;

        String requestBody = getContentAsString(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
        String responseBody = getContentAsString(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());

        log.info("REST CALL DETAILS: METHOD={}; URI={}; REQUEST_BODY={}; RESPONSE_CODE={}; RESPONSE_BODY={}; TIME_TAKEN={}ms",
                request.getMethod(),
                request.getRequestURI(),
                requestBody,
                response.getStatus(),
                responseBody,
                timeTaken);

        responseWrapper.copyBodyToResponse();
    }

    private String getContentAsString(byte[] buf, String charsetName) {
        if (buf == null || buf.length == 0) return "";
        try {
            return new String(buf, 0, buf.length, charsetName);
        } catch (UnsupportedEncodingException ex) {
            return "Unsupported Encoding";
        }
    }
}
