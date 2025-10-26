#!/bin/bash

echo "Building Docker image for code execution..."

docker build -t shodh/code-executor -f src/main/docker/Dockerfile .

echo "Docker image built successfully!"
echo "Image name: shodh/code-executor"
