# Application Security Documentation

## Overview
This document provides a comprehensive overview of the security architecture implemented in this Spring Boot application. It is designed to help developers understand how authentication, authorization, and other security mechanisms work within the system.

## Table of Contents
1. [Authentication Flow](#authentication-flow)
2. [Authorization and Access Control](#authorization-and-access-control)
3. [JWT Token Management](#jwt-token-management)
4. [Password Management](#password-management)
5. [Account Verification](#account-verification)
6. [Password Reset Flow](#password-reset-flow)
7. [Security Configuration](#security-configuration)
8. [Rate Limiting](#rate-limiting)

## Authentication Flow

### Registration Process
1. User submits registration information (name, email, phone, national ID, password)
2. System validates the input data
3. Password is encrypted using BCrypt
4. User account is created with `enabled = false` status
5. System generates a 6-digit OTP and stores it in Redis (10-minute expiration)
6. OTP is sent to the user's email for account verification
7. User receives a 201 Created response with their account details

### Account Verification
1. User submits the OTP received via email
2. System verifies the OTP against the stored value in Redis
3. If valid, the user account is activated (`enabled = true`)
4. OTP is deleted from Redis after successful verification

### Login Process
1. User submits email and password
2. System authenticates credentials using Spring Security's AuthenticationManager
3. If valid, the system generates:
   - Access token (JWT, short-lived, 15 minutes)
   - Refresh token (JWT, long-lived, 7 days)
4. Access token is returned in the response body
5. Refresh token is stored as an HttpOnly secure cookie
6. User can now access protected resources using the access token

### Token Refresh
1. When the access token expires, the client can request a new one
2. The refresh token is automatically sent from the HttpOnly cookie
3. System validates the refresh token
4. If valid, a new access token is generated and returned

## Authorization and Access Control

### Role-Based Access Control
The application implements a simple role-based access control system with two roles:
- **ADMIN**: Administrative users with access to all endpoints
- **CUSTOMER**: Regular users with limited access

### Endpoint Security
- Public endpoints (no authentication required):
  - `/auth/login`
  - `/auth/register`
  - `/auth/verify-account`
  - `/auth/initiate-password-reset`
  - `/auth/reset-password`
  - `/auth/refresh`
  - Swagger/OpenAPI documentation endpoints
- Admin-only endpoints:
  - `/admin/**` (requires ADMIN role)
- Protected endpoints:
  - All other endpoints require authentication

## JWT Token Management

### Token Structure
JWT tokens contain the following claims:
- Subject: User ID (UUID)
- Email: User's email address
- Phone Number: User's phone number
- Role: User's role (ADMIN or CUSTOMER)
- Issued At: Token creation timestamp
- Expiration: Token expiration timestamp

### Token Validation
1. JwtAuthenticationFilter intercepts all requests
2. Extracts the JWT token from the Authorization header
3. Validates the token signature using the secret key
4. Checks token expiration
5. Extracts user information and sets up the SecurityContext
6. If validation fails, returns 401 Unauthorized

## Password Management

### Password Storage
- Passwords are never stored in plain text
- BCryptPasswordEncoder is used for password hashing
- Password validation is performed during login using Spring Security

### Password Reset
1. User requests a password reset by providing their email
2. System generates a 6-digit OTP and stores it in Redis (10-minute expiration)
3. OTP is sent to the user's email
4. User submits the OTP along with a new password
5. System verifies the OTP and updates the password if valid

## Account Verification

The application uses a One-Time Password (OTP) system for account verification:
1. 6-digit numeric OTPs are generated
2. OTPs are stored in Redis with a 10-minute expiration
3. OTPs are sent to the user's email
4. User must verify their account before it becomes active

## Password Reset Flow

1. User initiates password reset by providing their email
2. System generates an OTP and sends it to the user's email
3. User submits the OTP along with a new password
4. System verifies the OTP and updates the password
5. User can now log in with the new password

## Security Configuration

### CSRF Protection
- CSRF protection is disabled for the API as it uses stateless JWT authentication

### Session Management
- The application uses stateless session management
- No session data is stored on the server

### Security Headers
- Standard security headers are applied to all responses

### Exception Handling
- Custom exception handling for authentication and authorization failures
- Detailed error messages for development, generic messages for production

## Rate Limiting

The application implements rate limiting to prevent abuse:
- `auth-rate-limiter`: Limits authentication-related requests
- `otp-rate-limiter`: Limits OTP verification attempts

Rate limiting helps protect against brute force attacks and denial of service attempts.