# Render Deployment Guide - Step by Step

Follow these steps to deploy your Shodh Contest Platform on Render.

## Prerequisites

- GitHub account (already have)
- Render account (sign up at https://render.com - it's free!)

## Step 1: Build Docker Image for Code Execution

First, we need to build and push the code executor Docker image. Render needs access to this image.

### Option A: Use Docker Hub (Recommended)

1. **Create Docker Hub account** (if you don't have one):
   - Go to https://hub.docker.com
   - Sign up for free

2. **Build and push the image**:
   ```bash
   # Build the code executor image
   docker build -t YOUR_DOCKERHUB_USERNAME/shodh-code-executor -f backend/src/main/docker/Dockerfile backend/
   
   # Login to Docker Hub
   docker login
   
   # Push the image
   docker push YOUR_DOCKERHUB_USERNAME/shodh-code-executor
   ```

3. **Update backend code** to use your Docker Hub image:
   - Edit `backend/src/main/resources/application.properties`
   - Change `docker.image.name=shodh/code-executor` to `docker.image.name=YOUR_DOCKERHUB_USERNAME/shodh-code-executor`

---

## Step 2: Deploy Backend on Render

1. **Go to Render Dashboard**:
   - Visit https://dashboard.render.com
   - Sign up/Login with GitHub

2. **Create New Web Service**:
   - Click "New +" → "Web Service"
   - Connect your GitHub repository: `heyDev07/SHODH`

3. **Configure Backend Service**:
   - **Name**: `shodh-backend`
   - **Environment**: `Docker`
   - **Region**: Choose closest to you (Oregon recommended)
   - **Branch**: `main`
   - **Root Directory**: Leave empty (uses root)
   - **Dockerfile Path**: `backend/src/main/docker/Dockerfile.backend`
   - **Docker Context**: `backend`
   - **Start Command**: (Leave empty - uses Dockerfile CMD)

4. **Environment Variables** (click "Advanced" → "Add Environment Variable"):
   ```
   SPRING_PROFILES_ACTIVE=production
   JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   DOCKER_IMAGE_NAME=YOUR_DOCKERHUB_USERNAME/shodh-code-executor
   ```

5. **Deploy**:
   - Click "Create Web Service"
   - Wait for build to complete (~5-10 minutes)
   - Copy your backend URL (e.g., `https://shodh-backend.onrender.com`)

---

## Step 3: Deploy Frontend on Render

1. **Create New Web Service**:
   - Click "New +" → "Web Service"
   - Connect same GitHub repository: `heyDev07/SHODH`

2. **Configure Frontend Service**:
   - **Name**: `shodh-frontend`
   - **Environment**: `Node`
   - **Region**: Same as backend
   - **Branch**: `main`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Start Command**: `npm start`

3. **Environment Variables**:
   ```
   NEXT_PUBLIC_API_URL=https://shodh-backend.onrender.com/api
   NODE_ENV=production
   PORT=10000
   ```

4. **Deploy**:
   - Click "Create Web Service"
   - Wait for build to complete (~3-5 minutes)

---

## Step 4: Update CORS Settings

After deployment, update backend CORS to allow your frontend domain:

1. **Edit Backend CORS**:
   - Edit `backend/src/main/java/com/shodh/contest/controller/ContestController.java`
   - Update `@CrossOrigin(origins = "*")` to:
     ```java
     @CrossOrigin(origins = {"https://shodh-frontend.onrender.com", "http://localhost:3000"})
     ```

2. **Commit and Push**:
   ```bash
   git add backend/src/main/java/com/shodh/contest/controller/ContestController.java
   git commit -m "Update CORS for Render deployment"
   git push origin main
   ```

3. **Render will auto-redeploy** (takes ~5 minutes)

---

## Step 5: Enable Auto-Deploy

Both services are set to auto-deploy on push to `main` branch by default.

To verify:
- Go to each service → Settings → Auto-Deploy
- Ensure "Auto-Deploy" is ON

---

## Access Your Application

After deployment:
- **Frontend**: `https://shodh-frontend.onrender.com`
- **Backend API**: `https://shodh-backend.onrender.com/api`

---

## Important Notes

### Free Tier Limitations

Render free tier includes:
- **750 hours/month** (enough for 24/7 operation)
- Services may **spin down after 15 minutes** of inactivity
- First request after spin-down takes ~30 seconds to wake up

### Docker Image Considerations

Render needs Docker-in-Docker support. The backend Dockerfile includes Docker client installation.

If you encounter Docker issues:
1. Verify Docker image is publicly accessible on Docker Hub
2. Check that image name matches in environment variables
3. Ensure Docker socket is accessible (handled by Render)

### Database

Currently using H2 in-memory database. For production:
- Consider upgrading to PostgreSQL (Render offers free PostgreSQL)
- Update `application.properties` with PostgreSQL connection string

---

## Troubleshooting

### Backend won't start
- Check logs in Render dashboard
- Verify Docker image exists and is accessible
- Check environment variables are set correctly

### Frontend can't connect to backend
- Verify `NEXT_PUBLIC_API_URL` points to correct backend URL
- Check CORS settings in backend
- Wait a few minutes for services to fully start

### 502 Bad Gateway
- Service might be spinning up (wait 30 seconds)
- Check service logs in Render dashboard
- Verify Docker image is accessible

---

## Next Steps

1. ✅ Deploy backend
2. ✅ Deploy frontend  
3. ✅ Update CORS
4. 🎉 Test your deployed application!
5. (Optional) Add custom domain
6. (Optional) Set up PostgreSQL database

---

## Support

- Render Docs: https://render.com/docs
- Render Status: https://status.render.com
- Check logs: Render Dashboard → Your Service → Logs

