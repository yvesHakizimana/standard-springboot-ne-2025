package com.ne.template.auth;

import com.ne.template.auth.exceptions.InvalidJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@AllArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // Skip auth endpoints
        if (request.getServletPath().startsWith("/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Parse token
            String token = authHeader.substring(7);
            Jwt jwt = jwtService.parseToken(token);

            // Validate claims
            if (jwt.getUserId() == null || jwt.getRole() == null) {
                throw new InvalidJwtException("Missing token claims");
            }

            // Set authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            jwt.getUserId(),
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + jwt.getRole()))
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.debug("Authentication failed: {}", ex.getMessage());
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new InvalidJwtException(ex.getMessage())
            );
        }
    }
}
