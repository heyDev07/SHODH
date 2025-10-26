#!/bin/bash

# Deployment script for Shodh Contest Platform

echo "🚀 Starting deployment process..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to deploy backend
deploy_backend() {
    echo -e "${BLUE}Deploying Backend...${NC}"
    
    PLATFORM=$1
    
    case $PLATFORM in
        railway)
            echo "Deploying to Railway..."
            cd backend
            railway up
            ;;
        render)
            echo "Deploying to Render..."
            render deploy
            ;;
        heroku)
            echo "Deploying to Heroku..."
            cd backend
            git subtree push --prefix backend heroku main
            ;;
        *)
            echo "Building Docker image..."
            docker build -t shodh-backend -f backend/src/main/docker/Dockerfile.backend backend/
            ;;
    esac
}

# Function to deploy frontend
deploy_frontend() {
    echo -e "${BLUE}Deploying Frontend...${NC}"
    
    PLATFORM=$1
    
    case $PLATFORM in
        vercel)
            echo "Deploying to Vercel..."
            cd frontend
            vercel --prod
            ;;
        netlify)
            echo "Deploying to Netlify..."
            cd frontend
            npm run build
            netlify deploy --prod --dir=.next
            ;;
        *)
            echo "Building Docker image..."
            docker build -t shodh-frontend -f frontend/Dockerfile frontend/
            ;;
    esac
}

# Main menu
echo -e "${GREEN}=== Shodh Contest Platform Deployment ===${NC}"
echo ""
echo "Select deployment option:"
echo "1) Full stack with Docker Compose (Recommended for self-hosted)"
echo "2) Vercel (Frontend) + Railway (Backend)"
echo "3) Render (Full Stack)"
echo "4) Heroku (Full Stack)"
echo "5) Custom"
echo ""

read -p "Enter choice [1-5]: " choice

case $choice in
    1)
        echo -e "${YELLOW}Building Docker images...${NC}"
        docker build -t shodh/code-executor -f backend/src/main/docker/Dockerfile backend/
        docker-compose up -d --build
        echo -e "${GREEN}✅ Deployment complete!${NC}"
        echo "Frontend: http://localhost:3000"
        echo "Backend: http://localhost:8080"
        ;;
    2)
        deploy_backend railway
        deploy_frontend vercel
        echo -e "${GREEN}✅ Deployment complete!${NC}"
        ;;
    3)
        deploy_backend render
        deploy_frontend render
        echo -e "${GREEN}✅ Deployment complete!${NC}"
        ;;
    4)
        deploy_backend heroku
        deploy_frontend heroku
        echo -e "${GREEN}✅ Deployment complete!${NC}"
        ;;
    5)
        echo "Custom deployment"
        read -p "Backend platform: " backend_platform
        read -p "Frontend platform: " frontend_platform
        deploy_backend $backend_platform
        deploy_frontend $frontend_platform
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}✨ Deployment process completed!${NC}"

