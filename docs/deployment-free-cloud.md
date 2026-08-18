# Free Cloud Demo Deployment Guide - ResQFlow

This runbook describes how to deploy ResQFlow in a **Free Cloud Demo** environment using managed services, with Kafka disabled to fit within free-tier resource constraints.

---

## ☁️ Architecture Overview

The free cloud demo deploys components across standard free-tier managed cloud platforms:

```
           +-----------------------------------------+
           |           Vercel (Frontend)             |
           +--------------------+--------------------+
                                |
                                | (HTTPS / JSON API)
                                v
           +--------------------+--------------------+
           |            Render (Backend)             |
           |        (Spring Profile: cloud)          |
           +----------+-------------------+----------+
                      |                   |
                      |                   |
                      v                   v
        +-------------+----+        +-----+-------------+
        | Render Postgres  |        |   Upstash Redis   |
        |    (Database)    |        | (Metrics/Cache)   |
        +------------------+        +-------------------+
```

### 📭 Kafka Cloud Mode Fallback
* **Why Kafka is disabled**: Standard managed Kafka services do not offer free-tier endpoints that are reliable for continuous polling, and running a self-hosted broker exceeds Render's free CPU/RAM allocations.
* **How it works**: When the profile `cloud` is activated, Kafka auto-configuration is excluded via `spring.autoconfigure.exclude`. The application loads the `LoggingEventPublisher` instead of `KafkaEventPublisher` under the shared `DomainEventPublisher` abstraction.
* **Architecture Preservation**: The domain layer still publishes outbox events in a transaction. When the scheduler processes these events, they are logged and instantly dispatched in-memory to the `EventProcessor`. All Postgres audit logs are written, and Redis dashboard metrics update dynamically. This allows you to demo the full platform features without paying for a broker, while preserving the capability to connect to Kafka in other profiles (e.g. `dev`, `prod`).

---

## ⚙️ Environment Variables Registry

Configure the following environment variables on your cloud hosts:

### 1. Render (Backend Web Service)
Ensure the Spring profile is set to `cloud`. Render automatically exposes a dynamic `PORT` variable which Spring Boot binds to on interface `0.0.0.0`.

| Environment Variable | Example Value | Description |
| :--- | :--- | :--- |
| **`SPRING_PROFILES_ACTIVE`** | `cloud` | Activates the Render PostgreSQL, Upstash Redis, and Kafka-disabled setup |
| **`DATABASE_URL`** | `jdbc:postgresql://ep-xyz.render.com/resqflow` | Render PostgreSQL database JDBC URL (include `?sslmode=require`) |
| **`DATABASE_USERNAME`** | `resqadmin` | Database username |
| **`DATABASE_PASSWORD`** | `<secure_password>` | Database password |
| **`REDIS_HOST`** | `aws-xyz-upstash.redis.com` | Upstash Redis connection host |
| **`REDIS_PORT`** | `6379` | Upstash Redis connection port |
| **`REDIS_PASSWORD`** | `<upstash_password>` | Upstash Redis password |
| **`REDIS_SSL_ENABLED`** | `true` | Required to be `true` for secure Upstash Redis connections |
| **`CORS_ALLOWED_ORIGINS`** | `https://resqflow.vercel.app` | Your Vercel frontend URL |
| **`JWT_SECRET`** | `<cryptographic_secret>` | Secure Base64 256-bit JWT signing key |

### 2. Vercel (Frontend Next.js Client)
Vercel requires the API connection URL to be available during build-time compilation.

| Environment Variable | Example Value | Description |
| :--- | :--- | :--- |
| **`NEXT_PUBLIC_API_URL`** | `https://resqflow.onrender.com/api/v1` | URL of your deployed Render backend service |

---

## 🚀 Deployment Steps

### Step 1: Provision Render PostgreSQL
1. Create a **New PostgreSQL** database on Render.
2. Under connections, copy the **External Connection String**.
3. Prefix the string with `jdbc:` to format it correctly for Spring Boot (e.g., `jdbc:postgresql://...`).

### Step 2: Provision Upstash Redis
1. Create a free database on [Upstash Console](https://console.upstash.com/).
2. Copy the endpoint address host, port (`6379`), and password.

### Step 3: Deploy Backend on Render
1. Create a **New Web Service** pointing to your ResQFlow repository.
2. Select **Docker** as the runtime.
3. Edit the **Docker Command / File** to point to the backend:
   * Dockerfile Path: `Dockerfile.backend`
   * Build Context: `.`
4. Under **Environment**, input the environment variables from the registry above.
5. Deploy. Render will build and expose the backend.

### Step 4: Deploy Frontend on Vercel
1. Create a **New Project** pointing to your ResQFlow repository.
2. Set the **Root Directory** to `frontend`.
3. Select **Next.js** preset.
4. Input `NEXT_PUBLIC_API_URL` as an Environment Variable.
5. Deploy. Vercel will build and host the Next.js static site.
