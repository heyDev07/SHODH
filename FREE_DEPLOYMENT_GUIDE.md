# 🚀 Free Deployment Guide for Shodh Contest Platform

## Overview
This guide will help you deploy your coding contest platform for **FREE** using Render.com. The deployment includes:
- Backend API (Spring Boot)
- Frontend (Next.js)
- Code execution environment (Docker)

## Prerequisites
1. **Docker Hub Account**: Sign up at [hub.docker.com](https://hub.docker.com)
2. **GitHub Account**: Your code should be in a GitHub repository
3. **Render Account**: Sign up at [render.com](https://render.com) (free tier available)

## Step 1: Prepare Docker Image

### 1.1 Get Your Docker Hub Username
1. Go to [hub.docker.com](https://hub.docker.com)
2. Sign up or log in
3. Note your username (e.g., `myusername`)

### 1.2 Build and Push Docker Image
1. Edit the `deploy-docker.sh` script:
   ```bash
   nano deploy-docker.sh
   ```
2. Replace `YOUR_DOCKERHUB_USERNAME` with your actual Docker Hub username
3. Run the script:
   ```bash
   ./deploy-docker.sh
   ```

**Manual Commands (if script doesn't work):**
```bash
# Replace 'yourusername' with your Docker Hub username
docker build -t yourusername/shodh-code-executor -f backend/src/main/docker/Dockerfile backend/
docker login
docker push yourusername/shodh-code-executor
```

## Step 2: Deploy to Render

### 2.1 Create Backend Service
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

### 2.2 Create Frontend Service
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
   (Replace `your-backend-url` with your actual backend URL)

5. Click **"Create Web Service"**

### 2.3 Update Backend CORS
Once both services are deployed, update the backend CORS configuration:

1. Go to your backend service in Render
2. Click **"Environment"**
3. Add this environment variable:
   ```
   FRONTEND_URL=https://your-frontend-url.onrender.com
   ```

4. Or manually update the CORS in `ContestController.java`:
   ```java
   @CrossOrigin(origins = {"https://your-frontend-url.onrender.com", "http://localhost:3000"})
   ```

## Step 3: Test Your Deployment

### 3.1 Access Your Application
- **Frontend**: `https://your-frontend-url.onrender.com`
- **Backend API**: `https://your-backend-url.onrender.com/api`

### 3.2 Test the Contest
1. Go to your frontend URL
2. Enter contest ID: `CONTEST-001`
3. Enter any username
4. Try solving a problem and submitting code

## Step 4: Customize Your Deployment

### 4.1 Add Your Own Problems
1. Edit `backend/src/main/java/com/shodh/contest/config/DataInitializer.java`
2. Add new contests and problems
3. Push changes to GitHub
4. Render will automatically redeploy

### 4.2 Custom Domain (Optional)
1. In Render dashboard, go to your service
2. Click **"Settings"** → **"Custom Domains"**
3. Add your domain (requires DNS configuration)

## Troubleshooting

### Common Issues:

1. **"Contest not found"**
   - Check if backend is running
   - Verify CORS configuration
   - Check environment variables

2. **Code execution fails**
   - Verify Docker image is pushed correctly
   - Check backend logs in Render dashboard

3. **Frontend can't connect to backend**
   - Update `NEXT_PUBLIC_API_URL` environment variable
   - Check CORS configuration

4. **Build failures**
   - Check Render build logs
   - Verify all dependencies are in package.json/pom.xml

### Getting Help:
- Check Render logs in the dashboard
- Verify environment variables are set correctly
- Test API endpoints directly using curl or Postman

## Cost Breakdown
- **Render**: Free tier includes 750 hours/month
- **Docker Hub**: Free for public repositories
- **Total Cost**: $0/month

## Next Steps
1. Monitor your application usage
2. Set up custom domain if needed
3. Add more contests and problems
4. Consider upgrading to paid plans for higher limits

---

**🎉 Congratulations!** Your coding contest platform is now live and accessible worldwide!
