# AWS EC2 Deployment Guide - ResQFlow

This runbook describes how to deploy the ResQFlow production stack onto a single AWS EC2 virtual machine using Docker Compose.

---

## 1. AWS Security Group Configuration

Configure your EC2 Security Group rules to expose **only** the required frontend and backend ports to the public internet:

| Rule Type | Port | Protocol | Source | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Inbound** | `22` | TCP | `My IP` | Secure SSH management access |
| **Inbound** | `3000` | TCP | `0.0.0.0/0` | Public access to Next.js Web Interface |
| **Inbound** | `8080` | TCP | `0.0.0.0/0` | Public access to Spring Boot REST API |
| **Outbound** | `All` | All | `0.0.0.0/0` | Outbound updates and dependency fetches |

> [!WARNING]
> Do **NOT** expose ports `5432` (PostgreSQL), `6379` (Redis), or `9092` (Kafka) in your EC2 Security Groups. They communicate privately within the Docker bridge network.

---

## 2. Server Setup (Ubuntu 22.04 LTS)

### Step A: Install Docker & Git
SSH into your EC2 instance and run:
```bash
sudo apt-get update && sudo apt-get upgrade -y

# Install Docker
sudo apt-get install -y docker.io docker-compose git

# Enable Docker daemon
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
```
*(Log out and log back in for group changes to take effect).*

### Step B: Clone the Repository
```bash
git clone https://github.com/snehasuresh2005/ResQFlow.git
cd ResQFlow
```

---

## 3. Configure the Production Environment

### Step A: Create the Production Environment File
Create a `.env` file at the repository root. Do **not** commit this file to Git.
```bash
cp .env.production.example .env
nano .env
```

### Step B: Configure the Variables
Fill in the placeholders in `.env`:
* **`POSTGRES_PASSWORD`**: A secure, randomly generated alphanumeric database password.
* **`JWT_SECRET`**: A cryptographically secure 256-bit Base64-encoded secret key. You can generate one via:
  ```bash
  openssl rand -base64 32
  ```
* **`CORS_ALLOWED_ORIGINS`**: `http://<EC2_PUBLIC_IP>:3000`
* **`NEXT_PUBLIC_API_URL`**: `http://<EC2_PUBLIC_IP>:8080/api/v1`

---

## 4. Run the Production Stack

### Step A: Build the Container Images
Build the backend Spring Boot app and Next.js client bundles from the repository root context:
```bash
docker compose -f docker-compose.prod.yml build
```

### Step B: Start all Services
Run in detached daemon mode:
```bash
docker compose -f docker-compose.prod.yml up -d
```

### Step C: Verify Services
Confirm all five containers are running and healthy:
```bash
docker compose -f docker-compose.prod.yml ps
```

---

## 5. Operations & Logs Checking

* **View live logs**:
  ```bash
  docker compose -f docker-compose.prod.yml logs -f
  ```
* **View backend logs**:
  ```bash
  docker compose -f docker-compose.prod.yml logs -f backend
  ```
* **Actuator Health check**:
  ```bash
  curl http://localhost:8080/actuator/health
  ```
* **Stopping the application** (without wiping data volumes):
  ```bash
  docker compose -f docker-compose.prod.yml down
  ```
* **Updating to a new version**:
  ```bash
  git pull
  docker compose -f docker-compose.prod.yml build --no-cache
  docker compose -f docker-compose.prod.yml up -d
  ```

---

## 6. Recommended Future HTTPS Architecture (Nginx + SSL)

For actual enterprise hosting, avoid exposing ports `3000` and `8080` directly. Place them behind a reverse proxy:

```
                  Client (Web Browser)
                           |
                     HTTPS (Port 443)
                           v
              Nginx Web Server (SSL Terminated)
                           |
           +---------------+---------------+
           | (Proxy Pass)                  | (Proxy Pass)
           v                               v
    Next.js Client (Port 3000)      Spring Boot API (Port 8080)
```
1. Run **Nginx** directly on the EC2 host.
2. Bind frontend and backend ports (`3000` and `8080`) only to `127.0.0.1` inside Docker.
3. Obtain a free SSL certificate from **Let's Encrypt** via Certbot.
4. Route requests to `resqflow.yourdomain.com` (Frontend) and `api.resqflow.yourdomain.com` (Backend API).
