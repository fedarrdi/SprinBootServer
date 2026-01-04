# Security Configuration Guide

## JWT Secret and Configuration

### Where Secrets Are Stored

**BEFORE (Bad Practice):**
```java
// Hardcoded in JwtService.java - BAD!
private static final String SECRET = "CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_256_BITS_MIN";
```

**AFTER (Good Practice):**
```properties
# In application.properties
jwt.secret=YOUR_RANDOM_SECRET_HERE
jwt.expiration=3600000
```

```java
// In JwtService.java - Injected from config
@Value("${jwt.secret}")
private String secret;

@Value("${jwt.expiration}")
private long expirationTime;
```

---

## Configuration Files Explained

### 1. `application.properties` (Current Active Config)
- Located at: `src/main/resources/application.properties`
- Contains your actual secrets (database password, JWT secret)
- **Currently NOT in .gitignore** (you can choose to ignore it or not)

### 2. `application.properties.example` (Template)
- Located at: `src/main/resources/application.properties.example`
- Template showing what values need to be set
- **Safe to commit to git** - contains no real secrets
- New developers copy this to `application.properties` and fill in real values

### 3. `application-local.properties` (Optional - For Local Dev)
- Create this for local development overrides
- **Already in .gitignore** - won't be committed
- Spring automatically loads this when you run with `--spring.profiles.active=local`

---

## Configuration Values

### jwt.secret
**What it is:**
- Secret key used to sign JWT tokens
- Must be at least 256 bits (32 characters) for HS256 algorithm
- Anyone with this secret can create fake tokens!

**How to generate a strong secret:**

```bash
# Option 1: Using OpenSSL
openssl rand -base64 32

# Option 2: Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"

# Option 3: Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"

# Option 4: Online generator (use at your own risk)
# https://generate-secret.vercel.app/32
```

**Example output:**
```
S8kZnX9mP2vR5qW8tY1uI4oP7lK0jH3gF6dA9sB2cM5n
```

### jwt.expiration
**What it is:**
- How long tokens are valid (in milliseconds)
- Default: 3600000 (1 hour)

**Common values:**
- 5 minutes: `300000`
- 15 minutes: `900000`
- 1 hour: `3600000`
- 1 day: `86400000`
- 7 days: `604800000`

**Recommendation:**
- Short-lived (1 hour) for security
- Use refresh tokens for longer sessions (future enhancement)

---

## Different Environments

### Development (Local)
```properties
# application.properties or application-local.properties
jwt.secret=dev-secret-key-at-least-32-chars-long-12345678
jwt.expiration=86400000
```

### Production
**Option 1: Environment Variables (RECOMMENDED)**
```bash
# Set environment variables
export JWT_SECRET="your-super-secret-production-key-generated-randomly"
export JWT_EXPIRATION=3600000

# Spring will read from environment if not in properties
```

**Option 2: application-prod.properties**
```properties
# application-prod.properties (in .gitignore)
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
```

Run with:
```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## Security Best Practices

### ✅ DO:
1. **Use different secrets for dev/production**
2. **Generate secrets randomly** (use tools above)
3. **Store production secrets in environment variables or secret managers**
4. **Rotate secrets periodically** (invalidates all tokens)
5. **Use short expiration times** (1 hour or less)
6. **Keep application.properties.example in git** as a template
7. **Document what values need to be set**

### ❌ DON'T:
1. **Don't hardcode secrets in source code**
2. **Don't commit real secrets to git**
3. **Don't use weak secrets** like "secret" or "password123"
4. **Don't use the same secret in dev and production**
5. **Don't share secrets in chat/email/Slack**
6. **Don't store secrets in frontend code** (anyone can read it)
7. **Don't make tokens valid forever**

---

## How Spring Loads Configuration

### Priority (highest to lowest):
1. **Environment variables** - `JWT_SECRET=xxx java -jar app.jar`
2. **Command line arguments** - `java -jar app.jar --jwt.secret=xxx`
3. **application-{profile}.properties** - `application-prod.properties`
4. **application.properties** - Default configuration
5. **@Value default values** - Fallback in code

### Example:
```java
// Will use environment variable JWT_SECRET if available
// Otherwise uses value from application.properties
// If neither exists, Spring will throw an error on startup
@Value("${jwt.secret}")
private String secret;

// Can provide default value (not recommended for secrets!)
@Value("${jwt.secret:default-secret-DO-NOT-USE}")
private String secret;
```

---

## Checking Your Configuration

### 1. On Application Startup
Spring will show loaded properties (if logging enabled):

```
Using property 'jwt.secret' = 'S8kZ...' (from application.properties)
Using property 'jwt.expiration' = '3600000' (from application.properties)
```

### 2. Add Logging (Optional)
```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        System.out.println("JWT Secret loaded: " + secret.substring(0, 10) + "...");
        System.out.println("JWT Expiration: " + expirationTime + "ms");
    }
}
```

---

## Migration Checklist

- [x] Move JWT secret from code to application.properties
- [x] Move JWT expiration from code to application.properties
- [x] Create application.properties.example template
- [x] Add application-local.properties to .gitignore
- [x] Update JwtService to use @Value injection
- [ ] Generate a strong random secret for production
- [ ] Set up environment variables for production deployment
- [ ] Remove default/example secrets before deploying
- [ ] Document configuration for your team

---

## Production Deployment Example

### Using Docker
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION=3600000
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/mydb
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
```

```bash
# .env file (in .gitignore!)
JWT_SECRET=your-super-secret-production-key-here
DB_PASSWORD=your-database-password-here
```

Run:
```bash
docker-compose up
```

---

## Troubleshooting

### Error: "Could not resolve placeholder 'jwt.secret'"
**Cause:** Spring can't find the property

**Solutions:**
1. Check application.properties exists and has `jwt.secret=...`
2. Check file is in `src/main/resources/`
3. Check for typos in property name
4. Rebuild project (`mvn clean install`)

### Error: "The specified key byte array is X bits which is not secure enough"
**Cause:** JWT secret is too short (less than 256 bits)

**Solution:** Use a secret that's at least 32 characters long

### Tokens from before still work after changing secret
**Cause:** Application wasn't restarted or using cached old value

**Solution:** Restart the application

---

## Summary

Your configuration is now **externalized** and **secure**:

1. **Secrets are in configuration files** - Easy to change per environment
2. **No hardcoded values** - Code is clean and reusable
3. **Example template provided** - New developers know what to configure
4. **gitignore configured** - Secrets won't accidentally be committed

**Next steps:**
1. Generate a strong random secret and update `application.properties`
2. Set up environment variables for production
3. Never commit real secrets to git!
