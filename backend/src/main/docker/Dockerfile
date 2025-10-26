FROM openjdk:17-slim

# Install necessary tools and languages
RUN apt-get update && apt-get install -y \
    bash \
    coreutils \
    python3 \
    python3-pip \
    nodejs \
    npm \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user for security
RUN groupadd -r coderunner && useradd -r -g coderunner coderunner

# Set working directory
WORKDIR /workspace

# Create directories for code execution
RUN mkdir -p /workspace/code /workspace/input /workspace/output \
    && chown -R coderunner:coderunner /workspace

# Set permissions
RUN chmod 755 /workspace

# Default command
CMD ["bash"]
