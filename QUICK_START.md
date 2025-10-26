# Quick Start Guide

Get the Shodh-a-Code Contest Platform running in 3 steps!

## Prerequisites Check

Make sure you have these installed:
```bash
# Check Java
java -version  # Should be 17 or higher

# Check Maven
mvn -version  # Should be 3.6 or higher

# Check Node.js
node -v  # Should be 18 or higher

# Check Docker
docker --version  # Docker Desktop should be running
```

## Steps to Run

### Step 1: Build Docker Image
```bash
cd backend
./build-docker.sh
```

Expected output:
```
Building Docker image for code execution...
[build output...]
Docker image built successfully!
Image name: shodh/code-executor
```

### Step 2: Start Backend
```bash
# If not already in backend directory
cd backend

# Start Spring Boot
mvn spring-boot:run
```

Expected output:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

Sample data initialized successfully!
Contest ID: CONTEST-001
Contest Name: Shodh Coding Challenge

Started ContestApplication in X.XXX seconds
```

### Step 3: Start Frontend
Open a **new terminal window**:
```bash
cd frontend
npm run dev
```

Expected output:
```
  ▲ Next.js 14.0.4
  - Local:        http://localhost:3000
  - Environments: .env.local

  ✓ Ready in X.XXXs
```

### Step 4: Access the Application

1. Open your browser to: **http://localhost:3000**
2. Enter contest ID: `CONTEST-001`
3. Enter your username
4. Click "Join Contest"
5. Start coding! 🚀

## Testing Your Setup

### Test 1: Verify Backend API
Open in browser: http://localhost:8080/api/contests/CONTEST-001

Should return JSON with contest details.

### Test 2: Verify Frontend
Open: http://localhost:3000

Should show the join page.

### Test 3: Submit a Solution
1. Join contest with ID: `CONTEST-001`
2. Select "Sum of Two Numbers" problem
3. Use this code:
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        System.out.println(a + b);
    }
}
```
4. Click "Submit Code"
5. Wait for status to change to "ACCEPTED" ✅

## Troubleshooting

### Docker Issues
```bash
# Make sure Docker is running
docker ps

# If it's not running, start Docker Desktop
# Then try building again:
cd backend
./build-docker.sh
```

### Port Already in Use
```bash
# Backend port 8080 in use?
# Change in backend/src/main/resources/application.properties
server.port=8081

# Frontend port 3000 in use?
# Next.js will automatically use 3001
```

### Maven Not Found
Install Maven:
```bash
# macOS
brew install maven

# Ubuntu/Debian
sudo apt-get install maven

# Verify installation
mvn -version
```

### Node Modules Issues
```bash
# Clean install
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## Stopping the Application

Press `Ctrl+C` in both terminal windows to stop the services.

## Clean Restart

If you need to start fresh:
```bash
# Stop all services (Ctrl+C)

# Clean Maven build
cd backend
mvn clean

# Rebuild Docker image
./build-docker.sh

# Restart services following Steps 2 & 3 above
```

## Need Help?

See the full documentation in:
- `README.md` - Complete project documentation
- `CONTRIBUTING.md` - Detailed setup guide
- `PROJECT_SUMMARY.md` - Technical overview

Happy coding! 🎉
