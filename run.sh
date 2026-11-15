#!/bin/bash
# Procura Launch Script
# Verifies build artifacts, creates data/graph directories, checks dependencies, and launches desktop GUI.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JAR_FILE="$PROJECT_ROOT/target/procura-1.0.0.jar"

echo "=========================================================="
echo "      Procura — CPU Monitor & Job Scheduling Desktop      "
echo "=========================================================="

if [ ! -f "$JAR_FILE" ]; then
    echo "[INFO] Compiled JAR not found at $JAR_FILE"
    echo "       Initiating automated build process first..."
    "$SCRIPT_DIR/build.sh"
fi

# Ensure data and graph directories exist
mkdir -p "$PROJECT_ROOT/data" "$PROJECT_ROOT/graphs" "$PROJECT_ROOT/profiling"

# Verify Python visualization capability
if command -v python3 &> /dev/null; then
    if ! python3 -c "import matplotlib, pandas" 2>/dev/null; then
        echo "[WARN] Matplotlib or Pandas not detected."
        echo "       To enable in-app plotting: pip3 install -r python/requirements.txt"
    fi
fi

echo ""
echo "[INFO] Launching Procura Desktop Application on Java Event Dispatch Thread..."
java -jar "$JAR_FILE"
