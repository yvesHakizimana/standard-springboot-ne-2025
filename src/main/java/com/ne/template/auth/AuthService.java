package com.ne.template.auth;


import com.ne.template.auth.dtos.LoginRequestDto;
import com.ne.template.auth.dtos.LoginResponse;
import com.ne.template.users.User;
import com.ne.template.users.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public User getCurrentUser(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        var userId = (UUID) authentication.getPrincipal();

        return userRepository.findById(userId).orElse(null);
    }

    public LoginResponse login(LoginRequestDto loginRequest, HttpServletResponse response){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        var user =  userRepository.findByEmail(loginRequest.email()).orElseThrow();

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(60 * 60 * 24 * 7); // 30 days
        cookie.setSecure(true);
        response.addCookie(cookie);

        return new LoginResponse(
                accessToken.toString()
        );
    }

    public String refreshAccessToken(String refreshToken){
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired()) {
            throw new BadCredentialsException("Refresh token is missing.");
        }
        var user = userRepository.findById(jwt.getUserId()).orElseThrow();
        return jwtService.generateAccessToken(user).toString();
    }
}
