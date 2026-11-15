# Procura — CPU Monitor and Job Scheduler

## Overview
Procura is an integrated systems application designed for Linux operating systems that combines live kernel-level process monitoring with an interactive CPU job scheduling simulator. Built as an undergraduate Computer Science & Engineering capstone project, Procura interfaces directly with the Linux `/proc` pseudo-filesystem to extract real-time process metadata, memory metrics, and CPU utilization statistics. In parallel, it implements classic operating system CPU scheduling algorithms—First-In-First-Out (FIFO/FCFS), Non-Preemptive Priority, and Round Robin (RR)—enabling users to model, execute, and analyze job workloads under deterministic parameters. The system bridges core Java concurrency and Swing desktop visualization with Python-powered Matplotlib analytical plotting through automated CSV data pipelining. Through this hybrid design, Procura demystifies abstract operating system principles into tangible, observable computing phenomena.

---

## Problem Statement
Operating system concepts such as process control blocks (PCBs), process hierarchies, context switches, and CPU scheduling algorithms are fundamental to computer science curricula, yet students often encounter them solely as theoretical abstractions or mathematical exercises on paper. Standard production utilities like `top`, `htop`, and `ps` provide real-time process monitoring but operate as static displays without exposing how scheduling algorithms determine process execution orders or how parent-child relationships evolve dynamically. Conversely, theoretical simulators rarely interface with living system resources or provide side-by-side metric evaluations. There is a demonstrable need for an integrated educational platform that couples live Unix process inspection with an interactive, multi-algorithm CPU scheduling and visualization engine.

---

## Objectives
1. **Real-Time Process Monitoring**: Query and parse the Linux `/proc` pseudo-filesystem to extract real-time metrics including Process ID (PID), Parent Process ID (PPID), process state, CPU percentage, Resident Set Size (RSS), and command names.
2. **Process Hierarchy Visualization**: Construct and visualize complete Unix parent-child process relationship trees anchored at `systemd`/`init` using interactive Java Swing tree components (`JTree`).
3. **Algorithm Simulation Engine**: Implement deterministic simulators for standard CPU scheduling policies: First-Come-First-Served (FCFS/FIFO), Priority Scheduling, and Round Robin (RR) with user-configurable time quanta.
4. **Comprehensive Metric Analytics**: Calculate rigorous scheduling performance metrics, specifically Arrival Time, Burst Time, Completion Time, Turnaround Time (TAT), Waiting Time (WT), Response Time (RT), CPU Throughput, and CPU Utilization.
5. **Cross-Language Analytical Plotting**: Pipeline simulation output through structured CSV serialization to Python scripts utilizing Matplotlib to generate comparative performance charts and visual Gantt timelines.
6. **Thread-Safe, Non-Blocking Architecture**: Implement asynchronous monitoring and simulation loops using Java's `ScheduledExecutorService` and `SwingWorker`, strictly adhering to Swing's Event Dispatch Thread (EDT) safety model.

---

## Features
- **Live System Telemetry Dashboard**:
  - Live process table auto-refreshed at configurable intervals.
  - Per-process CPU utilization calculated via the delta sample method from `/proc/[pid]/stat`.
  - Memory consumption metrics derived from `/proc/[pid]/statm` and `/proc/meminfo`.
  - Direct PID search, filtering by process name, and sorting across any metric column.
- **Unix Process Tree Inspector**:
  - Full tree visualization of all active processes branching from PID 1 (`systemd`/`init`).
  - Visual distinction between root services, user processes, and child threads.
  - Collapsible and expandable hierarchy nodes displaying child counts and aggregate resource footprints.
- **Interactive CPU Job Scheduler**:
  - Support for custom job workload creation (PID, Arrival Time, Burst Time, Priority).
  - Pre-loaded benchmark scenarios (CPU-bound, mixed I/O burst patterns, edge-case arrival times).
  - Algorithm implementations:
    - **FIFO / FCFS** (First-In, First-Out)
    - **Priority Scheduling** (Non-preemptive, lower numerical value = higher priority)
    - **Round Robin** (Configurable time quantum $\tau$)
  - Textual and visual Gantt chart sequence renderers.
- **Python Plotting Integration**:
  - Automated invocation of Python scripts via Java `ProcessBuilder`.
  - Export of simulation metrics to CSV format.
  - High-resolution Matplotlib graphs: Waiting Time vs. Jobs, Turnaround Time vs. Jobs, Algorithm Comparison Bar Charts, and Gantt charts saved as PNG artifacts.
- **Ergonomic Dark GUI**:
  - Custom dark theme designed for reduced eye strain during extended lab sessions.
  - Real-time status bar reporting system load, active thread count, and scheduler status.

---

## Technologies Used

| Technology | Purpose |
| :--- | :--- |
| **Java 17 (LTS)** | Core programming language; process monitoring, scheduling algorithms, model abstractions |
| **Java Swing & AWT** | Desktop graphical user interface (`JFrame`, `JTable`, `JTree`, custom dark components) |
| **Linux `/proc` Filesystem** | Virtual kernel filesystem parsed directly for real-time CPU and memory telemetry |
| **Java Concurrency (`java.util.concurrent`)** | `ScheduledExecutorService` for periodic polling; `SwingWorker` for background execution |
| **Python 3.8+** | Statistical data processing and external graphics rendering |
| **Matplotlib** | Generation of comparative algorithm charts, Gantt visual timelines, and metric distributions |
| **Apache Maven** | Build automation, dependency management, and packaging lifecycle |
| **JUnit 5** | Automated unit testing for scheduling algorithms, math verification, and proc parsers |
| **JDK Flight Recorder (JFR)** | Low-overhead Java application performance profiling and execution analysis |
| **Git & GitHub** | Source code version control, branch management, and collaborative development |

---

## Course Modules Incorporated (Course Topic Compliance)

As per the evaluation criteria, the project must incorporate at least **five topics** from the modules covered in the Operating Systems course (excluding LaTeX, Git, and Bash commands). Procura incorporates **six core course modules**:

1. **Module 1 — Unix Process Hierarchy**: Reconstruction of system-wide parent-child relationships using PID/PPID pairs starting from root PID 1 (`init`/`systemd`). Handles terminated parents by reparenting orphaned processes to PID 1. Visualized via an interactive `JTree`.
2. **Module 2 — Process Monitoring & Linux `/proc` Virtual Filesystem**: Kernel pseudo-filesystem traversal (`/proc/[pid]/stat`, `/proc/[pid]/cmdline`, `/proc/meminfo`). Calculates dynamic CPU usage using temporal differential sampling ($\Delta \text{ticks} / \Delta \text{time}$) and physical memory footprint (RSS).
3. **Module 3 — Java Concurrency & Multithreading**: Background thread pooling via `ScheduledExecutorService` (2 worker threads) to isolate kernel I/O from the GUI. Guarantees Event Dispatch Thread (EDT) thread safety using `SwingUtilities.invokeLater()` and asynchronous `SwingWorker` tasks.
4. **Module 4 — CPU Job Scheduling Algorithms**: Complete discrete-event implementations of:
   - **FIFO / FCFS** (First-In, First-Out; non-preemptive)
   - **Priority Scheduling** (Non-preemptive; lowest numerical value = highest priority; arrival tie-breaker)
   - **Round Robin** (Preemptive time-slicing; configurable quantum $\tau$; FIFO ready-queue with arrival interleaving)
5. **Module 5 — Performance Statistics & Scheduling Metrics**: Mathematical evaluation of scheduling efficiency:
   - $\text{Turnaround Time (TAT)} = \text{Completion Time} - \text{Arrival Time}$
   - $\text{Waiting Time (WT)} = \text{Turnaround Time} - \text{Burst Time}$
   - $\text{Response Time (RT)} = \text{First Execution} - \text{Arrival Time}$
   - $\text{CPU Utilization} = (\sum \text{Burst Time} / \text{Simulation Time}) \times 100\%$
   - $\text{Throughput} = \text{Completed Jobs} / \text{Simulation Time}$
6. **Module 6 — Inter-Process Communication & Performance Plotting**: Structured CSV data pipeline between Java and Python via `ProcessBuilder`. Matplotlib generates comparative bar charts and Gantt execution charts.

*(In addition, LaTeX, Beamer, Git version control, and Bash automation scripts are fully utilized as required evaluation tools).*

---

## Required Course Concepts & Implementation

| Course Concept | Implementation in Procura | Relevant Source Files / Components |
| :--- | :--- | :--- |
| **Unix Process Hierarchy** | Traversal of parent-child relationships using PID/PPID pairs starting from root PID 1 (`init`/`systemd`). Rendered as an interactive Swing tree model. | `com.procura.monitor.ProcessHierarchy`, `com.procura.ui.HierarchyPanel`, `javax.swing.JTree` |
| **Process Monitoring** | Direct parsing of Linux kernel pseudo-files (`/proc/[pid]/stat`, `/proc/[pid]/cmdline`, `/proc/meminfo`). Calculates dynamic CPU percentages using time-differential sampling. | `com.procura.monitor.ProcessMonitor`, `com.procura.model.ProcessInfo` |
| **Java Concurrency & Threads** | Non-blocking telemetry updates using a dedicated background thread pool (`ScheduledExecutorService`). Safe GUI updates scheduled on the Event Dispatch Thread (EDT). | `java.util.concurrent.ScheduledExecutorService`, `javax.swing.SwingWorker`, `SwingUtilities.invokeLater` |
| **Python & Java Integration** | Cross-runtime interoperability where Java serializes job simulation outputs into CSV datasets and triggers Python analytical scripts via `ProcessBuilder`. | `com.procura.util.CsvExporter`, `com.procura.util.PythonBridge`, `python/plot_metrics.py` |
| **Graphical User Interface** | Multi-panel desktop application featuring a custom dark theme, live tables, interactive trees, and embedded metric displays. | `com.procura.ui.MainWindow`, `com.procura.ui.ProcessMonitorPanel`, `com.procura.ui.SchedulerPanel` |
| **Analytical Plotting** | Visualization of comparative performance metrics (Turnaround Time, Waiting Time, Gantt chart bars) generated as vector/raster PNG graphics. | `python/plot_metrics.py`, Matplotlib visualization pipeline |
| **Version Control & Quality** | Strict Git branching workflows, atomic commits, comprehensive JUnit 5 regression suites, and automated build scripts. | Git repository history, `src/test/java/com/procura/`, Maven surefire plugin |

---

## Project Architecture

```
+-------------------------------------------------------------------------------+
|                                Procura Desktop UI                             |
|  +--------------------------+  +--------------------+  +--------------------+  |
|  |   Process Monitor Panel  |  |  Hierarchy Panel   |  |  Scheduler Panel   |  |
|  |  (Live Table, Filters)   |  |   (JTree View)     |  | (Gantt, Controls)  |  |
|  +--------------------------+  +--------------------+  +--------------------+  |
+---------------------------------------+---------------------------------------+
                                        | (Swing EDT Updates)
+---------------------------------------v---------------------------------------+
|                              Application Core                                 |
|  +----------------------------------+   +-----------------------------------+ |
|  |     Process Monitor Service      |   |       CPU Scheduling Engine       | |
|  |  - ScheduledExecutorService      |   |  - FCFS / FIFO Scheduler          | |
|  |  - Delta CPU Calculator          |   |  - Priority Scheduler             | |
|  |  - Hierarchy Builder             |   |  - Round Robin Scheduler          | |
|  +-----------------+----------------+   +-----------------+-----------------+ |
+--------------------|--------------------------------------|-------------------+
                     | (Kernel Telemetry)                   | (Export Jobs/Metrics)
+--------------------v-------------------+    +-------------v-------------------+
|       Linux Operating System           |    |     Data & Analytics Bridge     |
|  /proc/[pid]/stat    /proc/meminfo     |    |  - CsvExporter (Java)           |
|  /proc/[pid]/statm   /proc/[pid]/cmdline|    |  - ProcessBuilder execution     |
+----------------------------------------+    |  - Python Bridge                |
                                              +-------------+-------------------+
                                                            | (CSV Data Stream)
                                              +-------------v-------------------+
                                              |       Python Plotting Engine    |
                                              |  - matplotlib / pandas          |
                                              |  - PNG Gantt & Metric Plots     |
                                              +---------------------------------+
```

---

## Folder Structure

```
Procura/
├── pom.xml                               # Maven project configuration and dependencies
├── README.md                             # Comprehensive project documentation
├── AI_USAGE.md                           # Formal AI usage and academic integrity declaration
├── scripts/
│   ├── build.sh                          # Compilation and packaging shell script
│   ├── run.sh                            # Application launcher script
│   └── profile.sh                        # Java Flight Recorder (JFR) profiling script
├── python/
│   ├── requirements.txt                  # Python dependencies (matplotlib, pandas)
│   └── plot_metrics.py                   # Python metric visualization script
├── screenshots/                          # UI screenshots and generated visual artifacts
│   └── README.md                         # Placeholder guide for UI screen captures
├── docs/
│   ├── report/
│   │   ├── main.tex                      # Full BTech academic project report (LaTeX)
│   │   └── references.bib                # BibTeX bibliographic references
│   └── presentation/
│       └── slides.tex                    # 10-slide academic presentation (Beamer)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── procura/
    │   │           ├── Main.java         # Application entry point
    │   │           ├── model/
    │   │           │   ├── ProcessInfo.java      # Process state and metric entity
    │   │           │   ├── Job.java              # Simulated job data model
    │   │           │   └── SchedulingResult.java # Aggregated metrics and Gantt timeline
    │   │           ├── monitor/
    │   │           │   ├── ProcessMonitor.java   # /proc filesystem parser and sampler
    │   │           │   └── ProcessHierarchy.java # Parent-child process tree constructor
    │   │           ├── scheduler/
    │   │           │   ├── Scheduler.java        # Strategy interface for schedulers
    │   │           │   ├── FifoScheduler.java    # FIFO/FCFS algorithm implementation
    │   │           │   ├── PriorityScheduler.java# Priority scheduling algorithm
    │   │           │   └── RoundRobinScheduler.java # Round Robin algorithm
    │   │           ├── ui/
    │   │           │   ├── MainWindow.java           # Primary JFrame container
    │   │           │   ├── ProcessMonitorPanel.java  # Live process table panel
    │   │           │   ├── HierarchyPanel.java       # Process tree visualizer panel
    │   │           │   ├── SchedulerPanel.java       # Job simulation and Gantt panel
    │   │           │   └── Theme.java                # Dark theme palette and styling
    │   │           └── util/
    │   │               ├── CsvExporter.java          # Simulation results CSV serialization
    │   │               └── PythonBridge.java         # Subprocess launcher for Python scripts
    │   └── resources/
    │       └── styles.properties         # UI configuration constants
    └── test/
        └── java/
            └── com/
                └── procura/
                    ├── FifoSchedulerTest.java        # Unit tests for FIFO simulation
                    ├── PrioritySchedulerTest.java    # Unit tests for Priority simulation
                    ├── RoundRobinSchedulerTest.java  # Unit tests for Round Robin simulation
                    └── ProcessMonitorTest.java       # Unit tests for parsing and metrics
```

---

## Installation Requirements
To build and execute Procura, the host environment must meet the following prerequisites:
- **Operating System**: Linux (Ubuntu 20.04/22.04 LTS or compatible Debian/RHEL-based distribution). Linux is mandatory for kernel `/proc` filesystem telemetry.
- **Java Development Kit (JDK)**: OpenJDK 17 or higher.
- **Build Tool**: Apache Maven 3.6.3 or higher.
- **Python**: Python 3.8 or higher with `pip3`.
- **System Permissions**: Standard user privileges for general processes; elevated (`sudo`) permissions may be optionally required to inspect certain kernel-owned processes.

---

## Quick Start

### 1. Clone the Repository
```bash
git clone <repo-url>
cd Procura
```

### 2. Configure Permissions and Build
Make the shell scripts executable and run the build script:
```bash
chmod +x scripts/*.sh
./scripts/build.sh
```

### 3. Launch Procura
```bash
./scripts/run.sh
```

---

## Python Setup
Procura automatically triggers Python scripts to render simulation performance plots. Install the required Python packages into your environment:
```bash
pip3 install -r python/requirements.txt
```
The dependencies in `python/requirements.txt` include:
```
matplotlib>=3.5.0
pandas>=1.3.0
numpy>=1.21.0
```

---

## Testing
Comprehensive unit tests validate the correctness of scheduling algorithms (FIFO, Priority, Round Robin), turnaround and waiting time math, and string parsers:
```bash
mvn test
```
Test results and Surefire XML reports will be generated in `target/surefire-reports/`.

---

## Profiling
To evaluate the memory footprint, CPU utilization, and thread contention of the Procura application itself, run the automated Java Flight Recorder (JFR) profiling script:
```bash
./scripts/profile.sh
```
This script launches Procura with active JVM flight recording flags enabled:
`-XX:StartFlightRecording=disk=true,dumponexit=true,filename=procura_profile.jfr,settings=profile`
The resulting `.jfr` capture file can be opened and analyzed using **JDK Mission Control (JMC)**.

---

## Screenshots
Visual captures of the Procura interface in operation can be found in the `screenshots/` directory:
- `screenshots/process_monitor.png`: Real-time system process inspection table.
- `screenshots/process_hierarchy.png`: Collapsible `JTree` view of the Unix parent-child hierarchy.
- `screenshots/scheduler_simulation.png`: CPU scheduling configuration and execution dashboard.
- `screenshots/gantt_chart.png`: Visual Gantt chart rendering of job execution sequences.
- `screenshots/python_metrics_plot.png`: Matplotlib chart comparing Turnaround Time and Waiting Time across algorithms.

*(Refer to [screenshots/README.md](file:///C:/Users/swaro/.gemini/antigravity/scratch/Procura/screenshots/README.md) for detailed descriptions and replacement guidelines.)*

---

## Limitations
1. **Operating System Dependent**: Procura relies on the Linux `/proc` pseudo-filesystem and Unix-specific process metrics. While the scheduling simulator runs on any platform, real-time process monitoring is disabled or non-functional on Windows and macOS.
2. **Access Permissions**: Linux kernel security models restrict non-root users from reading specific process directories (e.g., processes running under other user IDs or kernel worker threads). Unreadable entries are caught and reported as restricted.
3. **Simulated Scheduling vs. Kernel Scheduling**: The CPU scheduling engine is a discrete-event simulator intended for educational and algorithmic analysis. It does not replace the Linux kernel's internal Completely Fair Scheduler (CFS).
4. **Sampling Delta Required for CPU Utilization**: Accurately measuring instantaneous CPU percentage requires two discrete temporal samples of `/proc/[pid]/stat` separated by a finite time window $\Delta t$. Hence, CPU percentages reflect activity over the previous sampling interval rather than a continuous mathematical instant.

---

## Team Contributions

| Member | Roll Number | Assigned Area | Key Contributions |
| :--- | :--- | :--- | :--- |
| **Swaroop** | CS25BT006 | Process Monitoring and CPU/Memory Data Collection | Worked on collecting process information from Linux `/proc` files, including `/proc/[pid]/stat` and `/proc/meminfo`. Implemented CPU usage calculation using time differences and handled memory-related calculations and data formatting. |
| **Bunny** | CS25BT016 | Process Management, CPU Scheduling and Concurrency | Worked on the process hierarchy and implemented the CPU scheduling algorithms, including FIFO, Priority Scheduling and Round Robin. Also handled background monitoring tasks, thread management and scheduling performance calculations. |
| **Basha** | CS25BT059 | GUI Development, Visualization, Testing and Documentation | Worked on the Java Swing interface, including the different panels, tables and data display. Also handled CSV export, Python-based graph generation using Matplotlib, JUnit testing, build scripts, performance profiling and project documentation. |

---

## AI Usage Declaration
Generative AI tools were utilized during the conception, design, and documentation stages of this project in accordance with university academic integrity standards. For a complete audit of how AI tools were employed, the specific components assisted, student validation responsibilities, and verification procedures, refer to [AI_USAGE.md](file:///C:/Users/swaro/.gemini/antigravity/scratch/Procura/AI_USAGE.md).

---

## License
This project is licensed under the MIT License. See the `LICENSE` file for full terms and conditions.
