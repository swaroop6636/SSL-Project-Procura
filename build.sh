#!/bin/bash
# Procura Build Script
# Validates system requirements, checks JDK, Maven, and Python, then compiles the application.

set -e

echo "=========================================================="
echo "          Procura - Desktop Build & Verification         "
echo "=========================================================="

# Check Java Development Kit (JDK 17+)
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java is not installed or not in PATH."
    echo "        Please install OpenJDK 17+ (e.g., sudo apt install openjdk-17-jdk)"
    exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -n 1)
echo "[OK] Found Java Runtime: $JAVA_VER"

if ! command -v javac &> /dev/null; then
    echo "[ERROR] Java compiler (javac) not found. Please install a complete JDK (JDK 17+)."
    exit 1
fi
JAVAC_VER=$(javac -version 2>&1 | head -n 1)
echo "[OK] Found Java Compiler: $JAVAC_VER"

# Check Apache Maven
if ! command -v mvn &> /dev/null; then
    echo "[ERROR] Apache Maven is not installed or not in PATH."
    echo "        Please install Maven (e.g., sudo apt install maven)"
    exit 1
fi
MVN_VER=$(mvn --version | head -n 1)
echo "[OK] Found Maven: $MVN_VER"

# Check Python 3
if command -v python3 &> /dev/null; then
    PY_VER=$(python3 --version 2>&1)
    echo "[OK] Found Python: $PY_VER"
    if python3 -c "import matplotlib, pandas" 2>/dev/null; then
        echo "[OK] Python dependencies (matplotlib, pandas) are installed."
    else
        echo "[WARN] Python dependencies missing. Run: pip3 install -r python/requirements.txt"
    fi
else
    echo "[WARN] Python3 not found. Plotting features will fall back to CSV export mode."
fi

# Determine project directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo ""
echo "Cleaning and compiling project with Maven..."
mvn clean compile test package

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================================="
    echo " [SUCCESS] Build completed successfully!"
    echo " Executable JAR: target/procura-1.0.0.jar"
    echo " Launch using:   ./scripts/run.sh"
    echo "=========================================================="
else
    echo ""
    echo "[ERROR] Build failed. Please review compiler logs above."
    exit 1
fi
