package com.ne.template.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AuthenticationFailureHandler, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        handleSecurityExceptions(request, response, authException, status);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        handleSecurityExceptions(request, response, exception,  status);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, accessDeniedException.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }

    private void handleSecurityExceptions(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException, HttpStatus status) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(authException.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setInstance(URI.create(request.getRequestURI() + "?error"));

        response.setStatus(status.value());
        new ObjectMapper().writeValue(response.getWriter(), problemDetail);
    }
}
