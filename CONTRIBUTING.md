# Contributing to Shodh-a-Code

Thank you for your interest in contributing to the Shodh Contest Platform!

## Development Setup

### Prerequisites Checklist
- [ ] Java 17 or higher installed
- [ ] Maven 3.6+ installed
- [ ] Node.js 18+ and npm installed
- [ ] Docker installed and running
- [ ] Git installed

### Initial Setup

1. Clone the repository
2. Build the Docker image (required for code execution)
   ```bash
   cd backend
   ./build-docker.sh
   ```

3. Start the backend
   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. In a separate terminal, start the frontend
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Project Structure

```
shodh/
├── backend/
│   ├── src/main/
│   │   ├── java/com/shodh/contest/
│   │   │   ├── controller/    # REST API endpoints
│   │   │   ├── service/       # Business logic
│   │   │   ├── repository/    # Data access layer
│   │   │   ├── model/         # Entity classes
│   │   │   ├── dto/           # Data transfer objects
│   │   │   └── config/        # Configuration
│   │   ├── resources/         # Configuration files
│   │   └── docker/            # Dockerfile for execution environment
│   └── pom.xml
├── frontend/
│   ├── app/                   # Next.js pages
│   ├── lib/                   # Utilities and API client
│   └── package.json
└── README.md
```

## Testing the Application

1. Open http://localhost:3000
2. Use contest ID: `CONTEST-001`
3. Enter any username
4. Solve the problems and submit solutions

## Development Workflow

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## Important Notes

- The Docker image must be built before running the backend
- The backend must be running before starting the frontend
- The H2 database is in-memory and resets on restart
- Sample data is automatically populated on first run
