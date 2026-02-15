# lets-play

Spring Boot REST API with JWT auth and MongoDB. Provides auth, users, and products with role-based access control and consistent JSON error responses.

**Features**
- JWT authentication (`/auth/register`, `/auth/login`)
- Products CRUD with ownership checks
- Users endpoints with admin-only access
- Global error handling with a unified `ApiError` response
- MongoDB + Mongo Express via Docker
- HTTPS enabled with a self-signed certificate (dev)

**Tech Stack**
- Java 25, Spring Boot, Spring Security
- MongoDB
- JWT (jjwt)

**Requirements**
- Java 25
- Docker (for MongoDB) or a local MongoDB instance

**Configuration**
This project loads environment variables from `.env` via `spring.config.import`.



**JWT settings** (in `src/main/resources/application.properties`):
- `jwt.secret` must be at least 64 characters
- `jwt.expirationMinutes` controls token TTL

**HTTPS (dev)**
The app runs on HTTPS port `8443` with a local self-signed certificate.
Generate it once:
```bash
./generateCertificat.sh
```

**Run MongoDB (Docker)**
```bash
docker compose up -d
```
Mongo Express UI: `http://localhost:8082`

**Run the API**
```bash
./mvnw spring-boot:run
```
API base URL: `https://localhost:8443`

**API Endpoints**

Auth:
- `POST /auth/register`
- `POST /auth/login` → returns `{ "accessToken": "..." }`

Products:
- `GET /products` (public)
- `POST /products` (auth)
- `PUT /products/{id}` (auth, owner or admin)
- `DELETE /products/{id}` (auth, owner or admin)

Users:
- `GET /users` (admin)
- `GET /users/{id}` (admin)
- `GET /users/me` (auth)
- `DELETE /users/{id}` (admin)

**Auth Flow (quick example)**
```bash
# Register
curl -k -X POST https://localhost:8443/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Jane","email":"jane@demo.com","password":"password123"}'

# Login
TOKEN=$(curl -k -X POST https://localhost:8443/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"jane@demo.com","password":"password123"}' | jq -r .accessToken)

# Create product
curl -k -X POST https://localhost:8443/products \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ball","description":"Football size 5","price":29.99}'
```

**Error Format**
All errors are returned as:
```json
{
  "timestamp": "2026-02-15T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/products"
}
```

**Notes**
- CORS allows `http://localhost:3000` by default.
- `seed.admin.enabled` can be toggled in `application.properties`.
