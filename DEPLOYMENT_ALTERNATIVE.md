# 🚀 Alternative Free Deployment Guide for Shodh Contest Platform

## Current Issue
Docker Hub requires email verification before you can pull/push images. Here are alternative approaches:

## Option 1: Use Render's Built-in Docker Registry (Recommended)

### Step 1: Deploy Backend to Render
1. Go to [render.com](https://render.com) and sign in
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository
4. Configure the service:
   - **Name**: `shodh-backend`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `./backend/src/main/docker/Dockerfile.backend`
   - **Docker Context**: `./backend`
   - **Plan**: `Starter` (Free)

5. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=production
   JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   ```

6. **Health Check Path**: `/api/contests/CONTEST-001`

7. Click **"Create Web Service"**

### Step 2: Deploy Frontend to Render
1. Click **"New +"** → **"Web Service"**
2. Connect the same GitHub repository
3. Configure the service:
   - **Name**: `shodh-frontend`
   - **Environment**: `Node`
   - **Build Command**: `cd frontend && npm install && npm run build`
   - **Start Command**: `cd frontend && npm start`
   - **Plan**: `Starter` (Free)

4. **Environment Variables**:
   ```
   NEXT_PUBLIC_API_URL=https://your-backend-url.onrender.com/api
   ```

5. Click **"Create Web Service"**

## Option 2: Fix Docker Hub Authentication

### Step 1: Verify Docker Hub Email
1. Go to [hub.docker.com](https://hub.docker.com)
2. Check your email for verification link
3. Click the verification link
4. Try logging in again:
   ```bash
   docker login
   ```

### Step 2: Build and Push Image
Once verified, run:
```bash
./deploy-docker.sh
```

## Option 3: Use GitHub Container Registry (Alternative)

### Step 1: Enable GitHub Container Registry
1. Go to your GitHub repository settings
2. Enable GitHub Container Registry
3. Use GitHub's registry instead of Docker Hub

### Step 2: Update Dockerfile
Modify the backend Dockerfile to use GitHub's registry:
```dockerfile
FROM ghcr.io/devaha/shodh-code-executor:latest
```

## Current Status
- ✅ Backend and Frontend code ready
- ✅ Docker configuration ready
- ⏳ Docker Hub authentication pending
- ⏳ Deployment to Render pending

## Next Steps
1. **Immediate**: Deploy to Render using Option 1 (no Docker Hub needed)
2. **Later**: Fix Docker Hub authentication for future updates
3. **Test**: Verify the deployed application works

## Testing Your Deployment
Once deployed:
1. Go to your frontend URL
2. Enter contest ID: `CONTEST-001`
3. Enter any username
4. Try solving a problem

## Troubleshooting
- If code execution fails, check Render logs
- Verify environment variables are set correctly
- Check CORS configuration between frontend and backend

---

**Note**: The Docker Hub authentication issue doesn't prevent deployment. Render can build the Docker image directly from your GitHub repository.
