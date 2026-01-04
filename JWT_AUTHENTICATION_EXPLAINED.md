# JWT Authentication System - Detailed Explanation

This document explains how the JWT authentication system works in this Spring Boot application, line by line.

---

## Table of Contents
1. [Overview - How It All Works Together](#overview)
2. [User Entity](#user-entity)
3. [UserRepository](#userrepository)
4. [JwtService - Token Generation & Validation](#jwtservice)
5. [JwtAuthenticationFilter - The Interceptor](#jwtauthenticationfilter)
6. [SecurityConfig - Spring Security Setup](#securityconfig)
7. [AuthController - Login & Registration](#authcontroller)
8. [ProfileController - Protected Endpoint](#profilecontroller)
9. [Request Flow Diagrams](#request-flow-diagrams)

---

## Overview

### The Big Picture

```
User Registration/Login
    ↓
Generate JWT Token
    ↓
Client stores token
    ↓
Client sends token with every request
    ↓
Filter validates token
    ↓
Controller gets authenticated user
```

### Key Components:
- **User.java** - Database entity representing a user
- **UserRepository.java** - Database access for users
- **JwtService.java** - Creates and validates JWT tokens
- **JwtAuthenticationFilter.java** - Intercepts requests and validates tokens
- **SecurityConfig.java** - Configures Spring Security
- **AuthController.java** - Handles login/register
- **ProfileController.java** - Example of protected endpoint

---

## User Entity

**File:** `User.java`

### Purpose
Represents a user in the database. This is a JPA entity that maps to the `users` table.

### Line-by-Line Explanation

```java
@Entity
```
- Tells Spring/JPA: "This is a database entity"
- Spring will create a table for this class

```java
@Table(name = "users")
```
- Specifies the table name in the database
- Without this, table would be named "user" (class name)

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```
- `@Id` - This is the primary key
- `@GeneratedValue` - Auto-generate values (auto-increment)
- `GenerationType.IDENTITY` - Use database's auto-increment feature
- `Long id` - The unique identifier for each user

```java
@Column(nullable = false)
private String name;
```
- `@Column` - Maps to a database column
- `nullable = false` - This column cannot be NULL (required field)
- `String name` - User's name

```java
@Column(nullable = false, unique = true)
private String email;
```
- `unique = true` - No two users can have the same email
- Database will enforce this with a unique constraint

```java
@Column(nullable = false)
private String passwordHash;
```
- Stores the hashed password (NOT the plain text password!)
- Never store passwords in plain text!

### Getters/Setters
- Standard Java getters and setters
- JPA and Spring need these to access the fields

---

## UserRepository

**File:** `UserRepository.java`

### Purpose
Interface for database operations on User entities.

```java
public interface UserRepository extends JpaRepository<User, Long> {
```
- Extends `JpaRepository` - Spring Data JPA provides automatic implementations
- `<User, Long>` - Entity type is User, ID type is Long
- You don't write SQL - Spring generates it automatically!

```java
Optional<User> findByEmail(String email);
```
- **Method name = query!**
- Spring reads "findByEmail" and generates: `SELECT * FROM users WHERE email = ?`
- Returns `Optional<User>` because user might not exist
- `Optional` prevents null pointer exceptions

```java
boolean existsByEmail(String email);
```
- Spring generates: `SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)`
- Returns `true` if email exists, `false` otherwise
- Used to check if email is already registered

---

## JwtService

**File:** `JwtService.java`

### Purpose
Creates and validates JWT tokens. This is the heart of the authentication system.

### Line-by-Line Explanation

```java
@Service
```
- Tells Spring: "This is a service bean"
- Spring will create ONE instance and inject it where needed

```java
private static final String SECRET =
        "CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_256_BITS_MIN";
```
- **CRITICAL**: This secret is used to sign and verify tokens
- Must be at least 256 bits (32 characters) for HS256 algorithm
- Anyone with this secret can create fake tokens!
- **TODO**: Move this to application.yml or environment variable
- **NEVER** commit real secrets to git!

### generateToken() Method

```java
public String generateToken(User user) {
```
- Input: User object
- Output: JWT token as a String

```java
return Jwts.builder()
```
- Start building a JWT token
- Uses builder pattern (method chaining)

```java
.subject(user.getId().toString())
```
- **Subject** = the main identifier in the token
- We store the user's ID
- Convert Long to String (JWT subject must be String)
- Later we'll extract this to identify the user

```java
.claim("email", user.getEmail())
```
- Add a custom claim (key-value pair)
- Stores the user's email in the token
- You can add any data: `.claim("name", user.getName())`
- **Warning**: Don't store sensitive data - tokens are visible to clients!

```java
.issuedAt(new Date())
```
- When was this token created?
- Current timestamp
- Used for debugging and validation

```java
.expiration(new Date(System.currentTimeMillis() + 3600_000))
```
- When does this token expire?
- `System.currentTimeMillis()` = now in milliseconds
- `+ 3600_000` = add 1 hour (3,600,000 milliseconds)
- Token is valid for 1 hour from now

```java
.signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), Jwts.SIG.HS256)
```
- **Sign the token** with our SECRET key
- `Keys.hmacShaKeyFor(SECRET.getBytes())` - Convert secret string to cryptographic key
- `Jwts.SIG.HS256` - Use HMAC-SHA256 algorithm
- Signature prevents token tampering (anyone who modifies the token invalidates the signature)

```java
.compact();
```
- Build and return the final token as a String
- Result looks like: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20ifQ.signature`
- Three parts separated by dots: `header.payload.signature`

### validateToken() Method

```java
public Claims validateToken(String token) {
```
- Input: JWT token string
- Output: Claims (the data inside the token)
- Throws exception if token is invalid

```java
return Jwts.parser()
```
- Start building a JWT parser
- Parser decodes and validates tokens

```java
.verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
```
- Tell the parser which key to use for verification
- Must be the SAME secret used to sign the token
- If token was signed with different key → validation fails

```java
.build()
```
- Build the parser

```java
.parseSignedClaims(token)
```
- Parse the token and verify the signature
- **Checks:**
  1. Token format is valid
  2. Signature is valid (not tampered)
  3. Token is not expired
- If any check fails → throws exception

```java
.getPayload();
```
- Extract the claims (data) from the token
- Returns a `Claims` object containing subject, email, expiration, etc.

### getUserIdFromToken() Method

```java
public Long getUserIdFromToken(String token) {
    Claims claims = validateToken(token);
    return Long.parseLong(claims.getSubject());
}
```
- Validates token first
- Extracts the subject (user ID as String)
- Converts to Long
- Returns the user ID

### isTokenValid() Method

```java
public boolean isTokenValid(String token) {
    try {
        validateToken(token);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```
- Simple helper method
- Returns `true` if token is valid
- Returns `false` if token is invalid/expired
- Catches any exception from validation

---

## JwtAuthenticationFilter

**File:** `JwtAuthenticationFilter.java`

### Purpose
**THE MOST IMPORTANT CLASS!**

This filter runs BEFORE every request reaches your controllers. It:
1. Checks for JWT token in the request
2. Validates the token
3. Loads the user from database
4. Tells Spring Security "this user is authenticated"

### Line-by-Line Explanation

```java
@Component
```
- Tells Spring: "Create a bean of this class"
- Spring will auto-detect and register this filter

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
```
- Extends `OncePerRequestFilter` - Spring class that runs once per request
- Alternative: `GenericFilterBean` (but can run multiple times)

### Constructor

```java
private final JwtService jwtService;
private final UserRepository userRepository;

public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
}
```
- **Dependency Injection**
- Spring automatically injects JwtService and UserRepository
- We need these to validate tokens and load users

### doFilterInternal() Method

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
```
- **Called automatically for EVERY request**
- `request` - The incoming HTTP request
- `response` - The HTTP response we'll send
- `filterChain` - Other filters that need to run after this one

#### Step 1: Extract Authorization Header

```java
String authHeader = request.getHeader("Authorization");
```
- Get the `Authorization` header from the request
- Client sends: `Authorization: Bearer eyJhbGc...`
- `authHeader` = `"Bearer eyJhbGc..."`
- If header doesn't exist: `authHeader = null`

#### Step 2: Check Header Format

```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}
```
- **Check 1**: Is header present?
- **Check 2**: Does it start with "Bearer "?
- If either fails:
  - Continue to next filter WITHOUT setting authentication
  - Request will be blocked by Spring Security (401/403)
  - Return early (skip rest of method)

#### Step 3: Extract Token

```java
String token = authHeader.substring(7);
```
- Remove "Bearer " prefix
- `authHeader = "Bearer eyJhbGc..."`
- `token = "eyJhbGc..."` (removes first 7 characters)
- Now we have just the JWT token

#### Step 4: Try-Catch Block

```java
try {
```
- If token validation fails → catch the exception
- Don't crash the application if token is invalid

#### Step 5: Validate Token and Extract User ID

```java
Long userId = jwtService.getUserIdFromToken(token);
```
- Calls JwtService to:
  1. Parse the token
  2. Verify signature
  3. Check expiration
  4. Extract user ID from subject
- If token is invalid/expired → throws exception → jumps to catch

#### Step 6: Load User from Database

```java
User user = userRepository.findById(userId).orElse(null);
```
- Query database: `SELECT * FROM users WHERE id = userId`
- If user exists → `user` = User object
- If user doesn't exist → `user = null`
- **Why might user not exist?**
  - User was deleted but token is still valid
  - Token was created for non-existent user (attack attempt)

#### Step 7: Check User Exists and Not Already Authenticated

```java
if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
```
- **Condition 1**: User was found in database
- **Condition 2**: User is not already authenticated (safety check)
- Both must be true to proceed

#### Step 8: Create Authentication Token

```java
UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
                user,           // Principal (the authenticated user)
                null,           // Credentials (we don't need password here)
                new ArrayList<>()  // Authorities/Roles (empty for now)
        );
```
- Create Spring Security authentication object
- **Parameter 1 (user)**: The authenticated user object
- **Parameter 2 (null)**: Credentials (password not needed after authentication)
- **Parameter 3 (empty list)**: User's roles/permissions (you can add later)

#### Step 9: Set Authentication Details

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
```
- Add extra info: IP address, session ID, etc.
- Good for logging and auditing
- Optional but recommended

#### Step 10: Store Authentication in Security Context

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```
- **MOST IMPORTANT LINE!**
- Stores authentication in Spring Security's global context
- Now Spring knows: "This request is authenticated as this user"
- Controllers can access this via `@AuthenticationPrincipal`

#### Step 11: Catch Exceptions

```java
} catch (Exception e) {
    // Token is invalid, just continue without setting authentication
}
```
- If ANY exception occurs (invalid token, expired, parsing error):
  - Silently catch it
  - Don't set authentication
  - Request continues but unauthenticated
  - Spring Security will block it (401/403)

#### Step 12: Continue Filter Chain

```java
filterChain.doFilter(request, response);
```
- **CRITICAL**: Must always call this!
- Continues to the next filter or controller
- If you don't call this → request hangs forever!

---

## SecurityConfig

**File:** `SecurityConfig.java`

### Purpose
Configures Spring Security: which endpoints are public, which require authentication, and registers our JWT filter.

### Line-by-Line Explanation

```java
@Configuration
```
- Tells Spring: "This class contains configuration beans"
- Spring will process this at startup

```java
public class SecurityConfig {
```

### Constructor Injection

```java
private final JwtAuthenticationFilter jwtAuthenticationFilter;

public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
}
```
- Inject our custom JWT filter
- We'll add it to Spring Security's filter chain

### Password Encoder Bean

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- `@Bean` - Spring will manage this object
- `BCryptPasswordEncoder` - Industry-standard password hashing
- Used in AuthController to hash passwords before saving
- Used to verify passwords during login
- **Never store plain text passwords!**

### Security Filter Chain

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
```
- Configures how Spring Security handles requests
- Returns a `SecurityFilterChain` that Spring will use

#### Disable CSRF

```java
.csrf(csrf -> csrf.disable())
```
- CSRF = Cross-Site Request Forgery protection
- Disabled for REST APIs (we use JWT tokens instead)
- **If using cookies**: Enable CSRF and add CSRF token!

#### Disable Form Login

```java
.formLogin(form -> form.disable())
```
- Disables Spring's default login page
- We're using JWT, not session-based login

#### Disable Basic Auth

```java
.httpBasic(basic -> basic.disable())
```
- Disables HTTP Basic Authentication
- We're using JWT tokens instead

#### Add JWT Filter

```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```
- **CRITICAL**: Add our JWT filter BEFORE Spring's default authentication filter
- Order matters! JWT filter must run first to set authentication
- `UsernamePasswordAuthenticationFilter.class` - Spring's default auth filter

#### Authorization Rules

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/**").permitAll()  // Public endpoints
        .anyRequest().authenticated()  // Everything else requires auth
);
```
- **Rule 1**: `/api/v1/auth/**` is public (login, register)
  - `/**` = wildcard, matches `/api/v1/auth/login`, `/api/v1/auth/register`, etc.
- **Rule 2**: All other requests require authentication
  - If no JWT token → 401/403 error
  - If valid JWT token → request proceeds

#### Build and Return

```java
return http.build();
```
- Build the security configuration
- Spring uses this to secure your application

---

## AuthController

**File:** `AuthController.java`

### Purpose
Handles user registration and login. Returns JWT tokens.

### Line-by-Line Explanation

```java
@RestController
```
- This is a REST API controller
- Returns JSON, not HTML pages

```java
@RequestMapping("/api/v1/auth")
```
- Base path for all endpoints in this controller
- Methods will be at `/api/v1/auth/register`, `/api/v1/auth/login`, etc.

### Dependencies

```java
private final UserRepository userRepository;
private final JwtService jwtService;
private final PasswordEncoder passwordEncoder;
```
- Injected by Spring via constructor
- Need these to save users, generate tokens, hash passwords

### Register Endpoint

```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
```
- `@PostMapping` - HTTP POST method
- Full path: `POST /api/v1/auth/register`
- `@RequestBody` - Request body contains JSON
- `@Valid` - Validate the request (email format, password length, etc.)
- `RegisterRequest` - DTO containing name, email, password

#### Check Email Exists

```java
if (userRepository.existsByEmail(request.email())) {
    return ResponseEntity.badRequest().build();
}
```
- Check if email is already registered
- If yes → return 400 Bad Request (no body for now)
- **TODO**: Return error message in response body

#### Create User

```java
User user = new User();
user.setName(request.name());
user.setEmail(request.email());
user.setPasswordHash(passwordEncoder.encode(request.password()));
```
- Create new User object
- Set name and email from request
- **Hash the password** using BCrypt
- Never store plain text password!

#### Save to Database

```java
User savedUser = userRepository.save(user);
```
- Spring Data JPA generates: `INSERT INTO users ...`
- Returns the saved user (with auto-generated ID)

#### Generate JWT Token

```java
String token = jwtService.generateToken(savedUser);
```
- Create JWT token for the new user
- Token contains user ID and email

#### Build Response

```java
AuthResponse response = new AuthResponse(
        token,
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getName()
);

return ResponseEntity.ok(response);
```
- Create response object with token and user info
- Return 200 OK with JSON body
- Client stores this token for future requests

### Login Endpoint

```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> register(@RequestBody @Valid LogInRequest request) {
```
- **Note**: Method is named "register" but should be "login" (typo!)
- Full path: `POST /api/v1/auth/login`
- `LogInRequest` - DTO containing email and password

#### Find User by Email

```java
Optional<User> userOptional = userRepository.findByEmail(request.email());

if (userOptional.isEmpty()) {
    return ResponseEntity.status(401).build();
}

User user = userOptional.get();
```
- Query database for user with this email
- If not found → 401 Unauthorized (wrong email)
- If found → extract User object

#### Verify Password

```java
String hashedPassword = user.getPasswordHash();
boolean isPasswordCorrect = passwordEncoder.matches(request.password(), hashedPassword);

if (!isPasswordCorrect) {
    return ResponseEntity.status(401).build();
}
```
- Get stored password hash from database
- `matches()` - Check if plain text password matches hash
- BCrypt handles the comparison securely
- If wrong → 401 Unauthorized

#### Generate Token and Respond

```java
String token = jwtService.generateToken(user);

AuthResponse response = new AuthResponse(
        token,
        user.getId(),
        user.getEmail(),
        user.getName()
);

return ResponseEntity.ok(response);
```
- Generate JWT token for the user
- Return token and user info
- Client stores token for future requests

---

## ProfileController

**File:** `ProfileController.java`

### Purpose
Example of a protected endpoint that requires JWT authentication.

### Line-by-Line Explanation

```java
@RestController
@RequestMapping("/api/v1")
public class ProfileController {
```
- REST controller
- Base path: `/api/v1`

```java
@GetMapping("/profile")
public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {
```
- `@GetMapping` - HTTP GET method
- Full path: `GET /api/v1/profile`
- `@AuthenticationPrincipal User user` - **MAGIC HAPPENS HERE!**
  - Spring automatically gets the authenticated user from SecurityContext
  - The user was placed there by JwtAuthenticationFilter
  - If no authentication → user = null → Spring returns 401

```java
ProfileResponse response = new ProfileResponse(
        user.getId(),
        user.getName(),
        user.getEmail()
);

return ResponseEntity.ok(response);
```
- Create response with user data
- **Note**: Don't return password hash!
- Return 200 OK with JSON

---

## Request Flow Diagrams

### Registration Flow

```
Client
  ↓
POST /api/v1/auth/register
Body: { "name": "John", "email": "john@example.com", "password": "secret123" }
  ↓
AuthController.register()
  ↓
1. Check if email exists → No
2. Create User object
3. Hash password with BCrypt
4. Save to database → User ID = 123
5. Generate JWT token with user ID 123
  ↓
Response: { "token": "eyJhbGc...", "id": 123, "email": "john@example.com", "name": "John" }
  ↓
Client stores token
```

### Login Flow

```
Client
  ↓
POST /api/v1/auth/login
Body: { "email": "john@example.com", "password": "secret123" }
  ↓
AuthController.login()
  ↓
1. Find user by email → User found (ID = 123)
2. Verify password → BCrypt matches ✓
3. Generate JWT token with user ID 123
  ↓
Response: { "token": "eyJhbGc...", "id": 123, "email": "john@example.com", "name": "John" }
  ↓
Client stores token
```

### Protected Endpoint Flow

```
Client
  ↓
GET /api/v1/profile
Header: Authorization: Bearer eyJhbGc...
  ↓
JwtAuthenticationFilter.doFilterInternal()
  ↓
1. Extract header → "Bearer eyJhbGc..."
2. Extract token → "eyJhbGc..."
3. Validate token → Valid ✓
4. Extract user ID → 123
5. Load user from database → User found
6. Create authentication token
7. Store in SecurityContextHolder
  ↓
SecurityFilterChain
  ↓
Check: Is user authenticated? → Yes ✓
Allow request to continue
  ↓
ProfileController.getProfile(@AuthenticationPrincipal User user)
  ↓
Spring injects user from SecurityContextHolder
  ↓
Create ProfileResponse with user data
  ↓
Response: { "id": 123, "name": "John", "email": "john@example.com" }
  ↓
Client receives data
```

### Failed Authentication Flow

```
Client
  ↓
GET /api/v1/profile
Header: (no Authorization header)
  ↓
JwtAuthenticationFilter.doFilterInternal()
  ↓
1. Extract header → null
2. No "Bearer " prefix
3. Skip authentication
4. Continue to next filter
  ↓
SecurityFilterChain
  ↓
Check: Is user authenticated? → No ✗
  ↓
Spring Security blocks request
  ↓
Response: 401 Unauthorized or 403 Forbidden
  ↓
Client receives error
```

---

## Security Considerations

### Things to Improve:

1. **Move SECRET to environment variable**
   - Don't hardcode in source code
   - Use `application.yml` or `application.properties`
   - Use different secrets for dev/prod

2. **Add refresh tokens**
   - Current: Token expires in 1 hour → user must login again
   - Better: Use refresh token to get new access token

3. **Add token blacklist**
   - Current: Can't revoke tokens before expiration
   - Better: Store revoked tokens in Redis/database

4. **Add rate limiting**
   - Prevent brute force attacks on login endpoint

5. **Return proper error messages**
   - Current: Just 400/401 with no body
   - Better: `{ "error": "Email already exists" }`

6. **Add user roles/permissions**
   - Current: All authenticated users have same access
   - Better: Admin, User, Guest roles with different permissions

7. **Use HTTPS**
   - Tokens are sensitive - always use HTTPS in production

8. **Add CSRF protection if using cookies**
   - Current: Tokens in headers (safe from CSRF)
   - If storing in cookies: Add CSRF token

---

## Common Issues and Solutions

### Issue: 401 Unauthorized when calling /profile

**Possible causes:**
1. Token not sent in header
2. Header format wrong (must be `Authorization: Bearer <token>`)
3. Token expired
4. Token invalid (wrong signature)
5. User deleted from database

**Debug steps:**
1. Check request headers in browser/Postman
2. Verify token format
3. Check token expiration
4. Try logging in again to get fresh token

### Issue: Filter not running

**Possible causes:**
1. Filter not registered in SecurityConfig
2. Filter added in wrong order

**Solution:**
- Ensure `.addFilterBefore()` is called in SecurityConfig
- Filter must be BEFORE `UsernamePasswordAuthenticationFilter`

### Issue: User always null in controller

**Possible causes:**
1. Token not validated
2. Authentication not set in SecurityContext
3. Wrong annotation (should be `@AuthenticationPrincipal`)

**Debug steps:**
1. Add logging in filter to see if it runs
2. Check if `SecurityContextHolder.getContext().setAuthentication()` is called
3. Verify controller method signature

---

## Testing Checklist

- [ ] Register new user → Should return token
- [ ] Register with existing email → Should return 400
- [ ] Login with correct credentials → Should return token
- [ ] Login with wrong password → Should return 401
- [ ] Login with non-existent email → Should return 401
- [ ] Call /profile without token → Should return 401/403
- [ ] Call /profile with invalid token → Should return 401/403
- [ ] Call /profile with expired token → Should return 401/403
- [ ] Call /profile with valid token → Should return user data

---

## Conclusion

This JWT authentication system provides:
- Stateless authentication (no sessions)
- Secure password storage (BCrypt)
- Token-based authorization
- Protection for all endpoints except login/register

The key is understanding the filter chain:
1. Request comes in
2. Filter validates token
3. Filter sets authentication in SecurityContext
4. Controller gets authenticated user
5. Response sent back

Once you understand this flow, you can extend it with roles, permissions, refresh tokens, and more!
