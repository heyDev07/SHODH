# Deployment Guide

This guide covers deploying the Shodh Coding Contest Platform to various platforms.

## Prerequisites

- Docker installed (for local deployment)
- Git repository access
- Cloud platform accounts (as needed)

## Deployment Options

### Option 1: Vercel (Frontend) + Railway/Render (Backend) - Recommended

#### Frontend Deployment (Vercel)

1. **Push code to GitHub** (already done)

2. **Connect to Vercel**:
   - Go to [vercel.com](https://vercel.com)
   - Import your GitHub repository
   - Configure:
     - Root Directory: `frontend`
     - Framework Preset: Next.js
     - Build Command: `npm run build`
     - Output Directory: `.next`
     - Install Command: `npm ci`

3. **Environment Variables**:
   ```
   NEXT_PUBLIC_API_URL=https://your-backend-url.com/api
   ```

4. **Deploy**: Vercel will automatically deploy on push to main branch

#### Backend Deployment (Railway)

1. **Install Railway CLI**:
   ```bash
   npm install -g @railway/cli
   railway login
   ```

2. **Create New Project**:
   ```bash
   railway init
   ```

3. **Deploy**:
   ```bash
   cd backend
   railway up
   ```

4. **Environment Variables** (set in Railway dashboard):
   ```
   JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   SPRING_PROFILES_ACTIVE=production
   ```

5. **Build Docker Image** (for code execution):
   ```bash
   docker build -t shodh/code-executor -f backend/src/main/docker/Dockerfile backend/
   ```

---

### Option 2: Docker Compose (Self-Hosted)

Perfect for deploying on your own server/VPS.

1. **Clone repository**:
   ```bash
   git clone https://github.com/heyDev07/SHODH.git
   cd SHODH
   ```

2. **Build Docker images**:
   ```bash
   # Build code executor image
   docker build -t shodh/code-executor -f backend/src/main/docker/Dockerfile backend/
   
   # Build and start all services
   docker-compose up -d --build
   ```

3. **Access application**:
   - Frontend: http://localhost:3000
   - Backend: http://localhost:8080

4. **Production setup** (with reverse proxy):
   ```bash
   # Use nginx or traefik for production
   # Example nginx config:
   # - Frontend: example.com -> localhost:3000
   # - Backend API: api.example.com -> localhost:8080
   ```

---

### Option 3: Render (Full Stack)

#### Backend on Render

1. **Create Web Service**:
   - Go to [render.com](https://render.com)
   - New → Web Service
   - Connect GitHub repository
   - Settings:
     - Name: `shodh-backend`
     - Environment: Docker
     - Dockerfile Path: `backend/src/main/docker/Dockerfile.backend`
     - Start Command: `java -jar app.jar`

2. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=production
   JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   ```

#### Frontend on Render

1. **Create Static Site**:
   - New → Static Site
   - Connect GitHub repository
   - Settings:
     - Build Command: `cd frontend && npm install && npm run build`
     - Publish Directory: `frontend/.next`

2. **Environment Variables**:
   ```
   NEXT_PUBLIC_API_URL=https://shodh-backend.onrender.com/api
   ```

---

### Option 4: Heroku (Legacy but Reliable)

#### Backend Deployment

1. **Install Heroku CLI**:
   ```bash
   npm install -g heroku
   heroku login
   ```

2. **Create Heroku App**:
   ```bash
   heroku create shodh-backend
   ```

3. **Configure Buildpack**:
   ```bash
   heroku buildpacks:set heroku/java
   ```

4. **Deploy**:
   ```bash
   cd backend
   git subtree push --prefix backend heroku main
   ```

#### Frontend Deployment

1. **Create Heroku App**:
   ```bash
   heroku create shodh-frontend
   ```

2. **Set Buildpack**:
   ```bash
   heroku buildpacks:set heroku/nodejs
   ```

3. **Configure**:
   ```bash
   heroku config:set NEXT_PUBLIC_API_URL=https://shodh-backend.herokuapp.com/api
   ```

---

## Environment Variables

### Backend Required Variables:
```bash
# Database (if using external DB instead of H2)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/contestdb
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Docker settings
DOCKER_IMAGE_NAME=shodh/code-executor
DOCKER_CONTAINER_TIMEOUT_SECONDS=30
DOCKER_MEMORY_LIMIT_MB=512
DOCKER_CPU_LIMIT=1
```

### Frontend Required Variables:
```bash
NEXT_PUBLIC_API_URL=https://your-backend-url.com/api
```

---

## Docker Image Requirements

The backend requires access to Docker for code execution. Ensure:

1. **Docker-in-Docker** is enabled (for cloud platforms)
2. **Volume mounts** for Docker socket (`/var/run/docker.sock`)
3. **Build and push** the code executor image:
   ```bash
   docker build -t shodh/code-executor -f backend/src/main/docker/Dockerfile backend/
   docker push shodh/code-executor  # If using Docker Hub
   ```

---

## Production Checklist

- [ ] Set up environment variables
- [ ] Configure CORS for production domain
- [ ] Set up SSL certificates (HTTPS)
- [ ] Configure database (switch from H2 to PostgreSQL/MySQL)
- [ ] Set up monitoring and logging
- [ ] Configure Docker image registry
- [ ] Set up backup strategy
- [ ] Configure CDN for frontend assets
- [ ] Test deployment end-to-end
- [ ] Set up CI/CD pipeline

---

## Troubleshooting

### Backend won't start
- Check Java version (must be 17+)
- Verify environment variables
- Check Docker access permissions

### Frontend can't connect to backend
- Verify `NEXT_PUBLIC_API_URL` is set correctly
- Check CORS configuration in backend
- Ensure backend is accessible from frontend domain

### Docker execution fails
- Verify Docker socket is accessible
- Check Docker image is built and available
- Review container resource limits

---

## Support

For deployment issues, check:
- [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
- [README.md](./README.md)

