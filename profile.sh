#!/bin/bash
# Procura Java Flight Recorder (JFR) Profiling Script
# Starts Procura with JFR enabled to collect low-overhead CPU, memory, and thread diagnostic traces.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JAR_FILE="$PROJECT_ROOT/target/procura-1.0.0.jar"
PROFILE_DIR="$PROJECT_ROOT/profiling"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
JFR_FILE="$PROFILE_DIR/procura_profile_$TIMESTAMP.jfr"

echo "=========================================================="
echo "    Procura — Performance Profiling (Java Flight Recorder) "
echo "=========================================================="

if [ ! -f "$JAR_FILE" ]; then
    echo "[INFO] JAR file not found. Building project..."
    "$SCRIPT_DIR/build.sh"
fi

mkdir -p "$PROFILE_DIR"

echo "[INFO] Starting Procura with Java Flight Recorder recording..."
echo "[INFO] Destination recording file: $JFR_FILE"
echo "[INFO] Settings: profile (duration: 120s, low overhead < 2%)"
echo ""

java -XX:StartFlightRecording=filename="$JFR_FILE",duration=120s,settings=profile \
     -XX:+FlightRecorder \
     -jar "$JAR_FILE"

echo ""
echo "=========================================================="
echo "[SUCCESS] Profiling session completed!"
echo "Trace saved to: $JFR_FILE"
echo ""
echo "To analyze the trace file:"
echo " 1. Use JDK Mission Control (GUI):   jmc -open \"$JFR_FILE\""
echo " 2. Print event summary:             jfr summary \"$JFR_FILE\""
echo " 3. Print thread activity:           jfr print --events jdk.ThreadAllocationStatistics \"$JFR_FILE\""
echo " 4. Print execution samples:         jfr print --events jdk.ExecutionSample \"$JFR_FILE\""
echo "=========================================================="
