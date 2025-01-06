package org.start.app.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class RequestLogFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            
            logger.info("\nREQUEST LOG ====================================\n" +
                    "URI         : {}\n" +
                    "Method      : {}\n" +
                    "Headers     : {}\n" +
                    "Parameters  : {}\n" +
                    "Request body: {}\n" +
                    "Response    : {}\n" +
                    "Duration    : {}ms\n" +
                    "===============================================",
                    requestWrapper.getRequestURI(),
                    requestWrapper.getMethod(),
                    getHeaders(requestWrapper),
                    requestWrapper.getParameterMap(),
                    requestBody,
                    responseBody,
                    duration
            );
            
            // 复制响应内容到原始response
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
    }
} 