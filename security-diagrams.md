# Security Flow Diagrams

This document provides schematic diagrams illustrating the security flows in the application, focusing on the essential functions from Spring Security filters to controllers and error handling.

## Table of Contents
1. [Authentication Flow](#authentication-flow)
2. [JWT Token Validation Flow](#jwt-token-validation-flow)
3. [User Registration and Verification Flow](#user-registration-and-verification-flow)
4. [Error Handling Flow](#error-handling-flow)

## Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant SecurityFilterChain
    participant JwtAuthenticationFilter
    participant AuthController
    participant AuthService
    participant AuthenticationManager
    participant CustomUserDetailsService
    participant JwtService
    participant UserRepository

    Client->>AuthController: POST /auth/login (email, password)
    AuthController->>AuthService: login(loginRequest, response)
    AuthService->>AuthenticationManager: authenticate(UsernamePasswordAuthenticationToken)
    AuthenticationManager->>CustomUserDetailsService: loadUserByUsername(email)
    CustomUserDetailsService->>UserRepository: findByEmail(email)
    UserRepository-->>CustomUserDetailsService: User
    CustomUserDetailsService-->>AuthenticationManager: UserDetails
    AuthenticationManager-->>AuthService: Authentication
    AuthService->>UserRepository: findByEmail(email)
    UserRepository-->>AuthService: User
    AuthService->>JwtService: generateAccessToken(user)
    JwtService-->>AuthService: accessToken
    AuthService->>JwtService: generateRefreshToken(user)
    JwtService-->>AuthService: refreshToken
    AuthService-->>AuthController: LoginResponse(accessToken)
    AuthController-->>Client: 200 OK + accessToken + refreshToken cookie

    Note over Client,UserRepository: For subsequent requests with JWT
    
    Client->>SecurityFilterChain: Request with Authorization: Bearer {token}
    SecurityFilterChain->>JwtAuthenticationFilter: doFilterInternal(request, response, chain)
    JwtAuthenticationFilter->>JwtService: parseToken(token)
    JwtService-->>JwtAuthenticationFilter: Jwt
    JwtAuthenticationFilter->>SecurityFilterChain: Set Authentication in SecurityContext
    SecurityFilterChain-->>Client: Response
```

## JWT Token Validation Flow

```mermaid
flowchart TD
    A[Client Request] --> B{Has Authorization Header?}
    B -->|No| C[Skip Authentication]
    B -->|Yes| D{Header starts with 'Bearer '?}
    D -->|No| C
    D -->|Yes| E[Extract Token]
    E --> F[JwtService.parseToken]
    F --> G{Valid Signature?}
    G -->|No| H[Throw InvalidJwtException]
    G -->|Yes| I{Token Expired?}
    I -->|Yes| H
    I -->|No| J{Has Required Claims?}
    J -->|No| H
    J -->|Yes| K[Extract User ID and Role]
    K --> L[Create Authentication Object]
    L --> M[Set Authentication in SecurityContext]
    M --> N[Continue Filter Chain]
    H --> O[Clear SecurityContext]
    O --> P[Return 401 Unauthorized]
    
    classDef process fill:#f9f,stroke:#333,stroke-width:2px;
    classDef decision fill:#bbf,stroke:#333,stroke-width:2px;
    classDef endpoint fill:#bfb,stroke:#333,stroke-width:2px;
    classDef error fill:#fbb,stroke:#333,stroke-width:2px;
    
    class A,E,F,K,L,M,N,O process;
    class B,D,G,I,J decision;
    class C,P endpoint;
    class H error;
```

## User Registration and Verification Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant UserService
    participant OtpService
    participant EmailService
    participant UserRepository
    participant RedisTemplate
    
    Client->>AuthController: POST /auth/register (user data)
    AuthController->>UserService: createUser(registerRequest)
    UserService->>UserRepository: save(user with enabled=false)
    UserRepository-->>UserService: savedUser
    UserService-->>AuthController: userResponseDto
    AuthController->>OtpService: generateOtp(email, OtpType.VERIFY_ACCOUNT)
    OtpService->>RedisTemplate: set(key, otp, 10min)
    RedisTemplate-->>OtpService: success
    OtpService-->>AuthController: otp
    AuthController->>EmailService: sendAccountVerificationEmail(email, name, otp)
    EmailService-->>AuthController: success
    AuthController-->>Client: 201 Created + userResponseDto
    
    Note over Client,RedisTemplate: Account Verification
    
    Client->>AuthController: PATCH /auth/verify-account (email, otp)
    AuthController->>OtpService: verifyOtp(email, otp, OtpType.VERIFY_ACCOUNT)
    OtpService->>RedisTemplate: get(key)
    RedisTemplate-->>OtpService: storedOtp
    OtpService->>RedisTemplate: delete(key)
    RedisTemplate-->>OtpService: success
    OtpService-->>AuthController: true/false
    AuthController->>UserService: activateUserAccount(email)
    UserService->>UserRepository: findByEmail(email)
    UserRepository-->>UserService: user
    UserService->>UserRepository: save(user with enabled=true)
    UserRepository-->>UserService: savedUser
    UserService-->>AuthController: success
    AuthController-->>Client: 200 OK
```

## Error Handling Flow

```mermaid
flowchart TD
    A[Client Request] --> B[SecurityFilterChain]
    B --> C{Authentication Error?}
    C -->|Yes| D[SecurityExceptionHandler]
    C -->|No| E[Controller Method]
    E --> F{Business Logic Error?}
    F -->|Yes| G[GlobalExceptionHandler]
    F -->|No| H[Success Response]
    
    D --> I{Type of Error}
    I -->|AccessDeniedException| J[Return 403 Forbidden]
    I -->|AuthenticationException| K[Return 401 Unauthorized]
    I -->|InvalidJwtException| L[Return 401 Unauthorized with details]
    
    G --> M{Type of Error}
    M -->|BadRequestException| N[Return 400 Bad Request]
    M -->|MethodArgumentNotValidException| O[Return 400 with Validation Errors]
    M -->|Other Exceptions| P[Return 500 Internal Server Error]
    
    J --> Q[Client Receives Error Response]
    K --> Q
    L --> Q
    N --> Q
    O --> Q
    P --> Q
    H --> R[Client Receives Success Response]
    
    classDef process fill:#f9f,stroke:#333,stroke-width:2px;
    classDef decision fill:#bbf,stroke:#333,stroke-width:2px;
    classDef endpoint fill:#bfb,stroke:#333,stroke-width:2px;
    classDef error fill:#fbb,stroke:#333,stroke-width:2px;
    
    class A,B,E process;
    class C,F,I,M decision;
    class H,R endpoint;
    class D,G,J,K,L,N,O,P,Q error;
```

## Function-Level Security Flow

```mermaid
flowchart TD
    A[HTTP Request] --> B[DispatcherServlet]
    B --> C[SecurityFilterChain]
    
    C --> D[WebAsyncManagerIntegrationFilter]
    D --> E[SecurityContextPersistenceFilter]
    E --> F[HeaderWriterFilter]
    F --> G[LogoutFilter]
    G --> H[JwtAuthenticationFilter]
    
    H --> I{Valid JWT?}
    I -->|No| J[Continue without Authentication]
    I -->|Yes| K[Set Authentication in SecurityContext]
    
    J --> L[ExceptionTranslationFilter]
    K --> L
    
    L --> M[FilterSecurityInterceptor]
    M --> N{Has Required Authority?}
    N -->|No| O[AccessDeniedException]
    N -->|Yes| P[Controller Method]
    
    O --> Q[SecurityExceptionHandler]
    Q --> R[Return Error Response]
    
    P --> S{Method has @PreAuthorize?}
    S -->|Yes| T{Meets Authorization?}
    S -->|No| U[Execute Method]
    
    T -->|No| V[AccessDeniedException]
    T -->|Yes| U
    
    U --> W[Return Response]
    V --> X[GlobalExceptionHandler]
    X --> Y[Return Error Response]
    
    classDef filter fill:#f9f,stroke:#333,stroke-width:2px;
    classDef decision fill:#bbf,stroke:#333,stroke-width:2px;
    classDef endpoint fill:#bfb,stroke:#333,stroke-width:2px;
    classDef error fill:#fbb,stroke:#333,stroke-width:2px;
    
    class A,B,P,U,W endpoint;
    class C,D,E,F,G,H,L,M filter;
    class I,N,S,T decision;
    class J,K process;
    class O,Q,R,V,X,Y error;
```