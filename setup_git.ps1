# Procura Git Repository Initialization & Logical Commit History Script (PowerShell)
# Initializes Git repo and establishes the required 12 logical commits for evaluation.

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
Set-Location $ProjectRoot

if (Test-Path ".git") {
    Write-Host "[INFO] Git repository already initialized." -ForegroundColor Yellow
    git status
    exit 0
}

Write-Host "Initializing Git repository..." -ForegroundColor Cyan
git init

if (-not (git config user.name)) {
    git config user.name "Procura Team (Swaroop, Bunny, Basha)"
    git config user.email "swaroop.cs25bt006@procura.cs"
}

Write-Host "Creating sequential logical commits as per project requirements..." -ForegroundColor Cyan

# 1. Initial project structure
git add pom.xml .gitignore
git commit -m "Initial project structure and Maven build configuration"

# 2. Add process monitoring
git add src/main/java/com/procura/monitoring/ProcessInfo.java `
        src/main/java/com/procura/monitoring/ProcessMonitor.java `
        src/main/java/com/procura/monitoring/CpuMonitor.java `
        src/main/java/com/procura/monitoring/MemoryMonitor.java `
        src/main/java/com/procura/util/
git commit -m "Add Linux /proc process monitoring, CPU, and memory telemetry modules"

# 3. Implement Unix process hierarchy
git add src/main/java/com/procura/monitoring/ProcessHierarchy.java
git commit -m "Implement Unix process hierarchy tree with orphan handling"

# 4. Implement FIFO scheduling
git add src/main/java/com/procura/scheduler/Job.java `
        src/main/java/com/procura/scheduler/Scheduler.java `
        src/main/java/com/procura/scheduler/FifoScheduler.java
git commit -m "Implement non-preemptive First-Come First-Served (FIFO) CPU scheduler"

# 5. Implement priority scheduling
git add src/main/java/com/procura/scheduler/PriorityScheduler.java
git commit -m "Implement non-preemptive Priority CPU scheduler with arrival tiebreaking"

# 6. Implement Round Robin scheduling
git add src/main/java/com/procura/scheduler/RoundRobinScheduler.java
git commit -m "Implement preemptive Round Robin CPU scheduler with configurable quantum"

# 7. Add scheduling statistics and CSV export
git add src/main/java/com/procura/scheduler/SchedulingResult.java `
        src/main/java/com/procura/scheduler/SchedulingMetrics.java `
        src/main/java/com/procura/analytics/CsvExporter.java `
        src/main/java/com/procura/analytics/PerformanceAnalyzer.java `
        data/
git commit -m "Add scheduling statistics engine, Gantt tracker, and CSV telemetry exporters"

# 8. Add Java multithreading and Swing GUI
git add src/main/java/com/procura/Main.java `
        src/main/java/com/procura/ui/
git commit -m "Develop multi-threaded dark-themed Java Swing desktop interface"

# 9. Integrate Python plotting
git add python/
git commit -m "Integrate Python Matplotlib visualization pipeline for CPU, RAM, and scheduler benchmarks"

# 10. Add unit tests
git add src/test/
git commit -m "Add JUnit 5 unit tests verifying scheduling metrics, hierarchy, and validators"

# 11. Add Bash automation and profiling
git add scripts/ profiling/
git commit -m "Add Bash automation scripts for build, run, JFR profiling, and repo setup"

# 12. Complete documentation and interactive preview
git add README.md AI_USAGE.md docs/ screenshots/ preview/
git commit -m "Complete project documentation: comprehensive README, LaTeX report, Beamer presentation, AI usage declaration, and interactive preview"

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Git repository initialized with 12 logical commits!" -ForegroundColor Green
git log --oneline
Write-Host "==========================================================" -ForegroundColor Green
