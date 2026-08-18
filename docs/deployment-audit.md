# Deployment Audit - ResQFlow

This document captures the audit findings, architecture patterns, and configuration settings required to run ResQFlow in both development and single-host production Docker environments.

---

## 1. Environment Variable Registry

The system consumes these primary variables:

| Variable Name | Context / Module | Default (Development) | Purpose |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Backend | `dev` | Activates profile-specific properties (e.g. `application-prod.yml`) |
| `DATABASE_URL` | Backend (Prod) | `jdbc:postgresql://localhost:5433/resqflow` | PostgreSQL database connection string |
| `DATABASE_USERNAME` | Backend (Prod) | `postgres` | Database connection username |
| `DATABASE_PASSWORD` | Backend (Prod) | N/A (Required) | Database connection password |
| `REDIS_HOST` | Backend (Prod) | `localhost` | Redis host name |
| `REDIS_PORT` | Backend (Prod) | `6379` | Redis port number |
| `REDIS_PASSWORD` | Backend (Prod) | None | Redis connection password |
| `REDIS_SSL_ENABLED` | Backend (Prod) | `false` | Enable SSL for Redis connection |
| `KAFKA_BOOTSTRAP_SERVERS` | Backend (Prod) | `localhost:9092` | Kafka broker endpoints |
| `JWT_SECRET` | Backend (All) | `dGhpcy1pcy1hLXNlY3VyZS1hbmQtc3Ryb25nLWp3dC1zZWNyZXQta2V5LXRoYXQtbXVzdC1iZS0yNTYtYml0cw==` | Secret key used to sign and verify JWT tokens (Prod requires secure override) |
| `JWT_EXPIRATION_MS` | Backend (All) | `86400000` | Expiration time of generated JWTs in milliseconds |
| `CORS_ALLOWED_ORIGINS` | Backend (All) | `http://localhost:3000` | Whitelisted frontend client domains |
| `NEXT_PUBLIC_API_URL` | Frontend | `http://localhost:8080/api/v1` | Public API endpoint whitelisted for browser network requests |

---

## 2. Next.js environment variable resolution

In Next.js, environment variables prefixed with `NEXT_PUBLIC_` are **evaluated at build time**.
* Next.js inlines the value of `NEXT_PUBLIC_API_URL` directly into the client-side JavaScript bundle during the execution of `npm run build` inside the Docker build step.
* Consequently, passing `NEXT_PUBLIC_API_URL` as a runtime container environment variable (via `docker run` or the `environment` section of `docker-compose.yml`) will **not** update the frontend client code; the bundle will fall back to local dev values.
* **Resolution**: We pass the variable as a Docker build argument (`ARG NEXT_PUBLIC_API_URL`) during the image compilation phase. In `docker-compose.prod.yml`, this is specified under the `build.args` block.

---

## 3. Found Issues & Fixes

1. **CORS Security**: The default configuration allowed all origins (`*`) for CORS. In production, this is locked down to `CORS_ALLOWED_ORIGINS` passed dynamically.
2. **JWT Secret Key Safe-fail**: The default JWT secret was hardcoded as a fallback. We modified `application-prod.yml` to remove the default, guaranteeing that the backend fails to start if `JWT_SECRET` is missing.
3. **Double-run local port conflict**: In local development, the user runs Spring Boot and Next.js directly on the host OS but runs PostgreSQL, Redis, and Kafka inside Docker. `docker-compose.yml` maps dependencies to host ports, while `docker-compose.prod.yml` encapsulates them inside `resqflow-network` without host port exposure (except for backend `8080` and frontend `3000`).
4. **Security Hardening**: Multi-stage docker builds compile in isolated stages, copying only clean JARs/JS bundles into non-root runtime environments (`resquser` and `nextjs`).
