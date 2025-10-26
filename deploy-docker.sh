#!/bin/bash

# Docker Hub Deployment Script for Shodh Code Executor
# Replace 'YOUR_DOCKERHUB_USERNAME' with your actual Docker Hub username

DOCKERHUB_USERNAME="devaha"

echo "🚀 Building and deploying Docker image for code executor..."

# Check if username is set
if [ "$DOCKERHUB_USERNAME" = "YOUR_DOCKERHUB_USERNAME" ]; then
    echo "❌ Please set your Docker Hub username in this script first!"
    echo "Edit the DOCKERHUB_USERNAME variable at the top of this file."
    exit 1
fi

# Build the Docker image
echo "📦 Building Docker image..."
docker build -t $DOCKERHUB_USERNAME/shodh-code-executor -f backend/src/main/docker/Dockerfile backend/

if [ $? -ne 0 ]; then
    echo "❌ Docker build failed!"
    exit 1
fi

echo "✅ Docker image built successfully!"

# Login to Docker Hub
echo "🔐 Logging into Docker Hub..."
docker login

if [ $? -ne 0 ]; then
    echo "❌ Docker login failed!"
    exit 1
fi

# Push the image
echo "📤 Pushing image to Docker Hub..."
docker push $DOCKERHUB_USERNAME/shodh-code-executor

if [ $? -ne 0 ]; then
    echo "❌ Docker push failed!"
    exit 1
fi

echo "✅ Docker image pushed successfully!"
echo "🎉 Your code executor image is now available at: $DOCKERHUB_USERNAME/shodh-code-executor"
echo ""
echo "Next steps:"
echo "1. Go to https://render.com and sign up/login"
echo "2. Create a new Web Service"
echo "3. Connect your GitHub repository"
echo "4. Use the configuration from render.yaml"
echo "5. Set the Docker image to: $DOCKERHUB_USERNAME/shodh-code-executor"
