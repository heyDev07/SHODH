# Setup Instructions

## Critical: Java Version Configuration

This project requires **Java 17**. If you have multiple Java versions installed, make sure to set JAVA_HOME to Java 17 before running Maven commands.

### Check Java Version
```bash
java -version  # Should show version 17.x.x
```

### Set JAVA_HOME to Java 17 (macOS)
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "JAVA_HOME set to: $JAVA_HOME"
```

You can add this to your `~/.zshrc` or `~/.bash_profile` to make it permanent.

### Set JAVA_HOME to Java 17 (Linux)
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
# Or find Java 17 with: sudo update-alternatives --config java
```

### Set JAVA_HOME to Java 17 (Windows)
```powershell
# In PowerShell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
```

## Complete Setup Steps

1. **Install Prerequisites** (if not already installed)
   - Java 17 or higher
   - Maven 3.6+
   - Node.js 18+
   - Docker

2. **Set Java 17** (run before every Maven command)
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
   ```

3. **Start Backend**
   ```bash
   cd backend
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   mvn spring-boot:run
   ```

4. **Start Frontend** (in a new terminal)
   ```bash
   cd frontend
   npm run dev
   ```

5. **Access Application**
   - Open http://localhost:3000 in your browser
   - Use contest ID: `CONTEST-001`

## Troubleshooting

### Build Fails with Java Version Error
Make sure JAVA_HOME points to Java 17:
```bash
echo $JAVA_HOME  # Should show Java 17 path
```

### Lombok Getter/Setter Errors
This happens when Java 25 is used. Fix by setting JAVA_HOME:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean compile
```
