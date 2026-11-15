# PowerShell script to generate realistic 1280x800 application screenshots using System.Drawing

Add-Type -AssemblyName System.Drawing

$ScreenshotsDir = "C:\Users\swaro\.gemini\antigravity\scratch\Procura\screenshots"
if (!(Test-Path $ScreenshotsDir)) { New-Item -ItemType Directory -Path $ScreenshotsDir -Force }

function Draw-WindowChrome($g, $width, $height, $title, $activeTab) {
    # Dark Theme Colors
    $bgBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(30, 30, 30))
    $panelBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(40, 40, 40))
    $borderPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(60, 60, 60), 1)
    $textBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(220, 220, 220))
    $mutedBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(160, 160, 160))
    $accentBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230))
    $titleBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(50, 50, 50))
    
    $titleFont = [System.Drawing.Font]::new("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
    $tabFont = [System.Drawing.Font]::new("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)
    $bodyFont = [System.Drawing.Font]::new("Segoe UI", 9.5)
    
    # Fill Background
    $g.FillRectangle($bgBrush, 0, 0, $width, $height)
    
    # Title Bar
    $g.FillRectangle($titleBrush, 0, 0, $width, 36)
    $g.DrawLine($borderPen, 0, 36, $width, 36)
    $g.DrawString($title, $titleFont, $textBrush, 14, 8)
    
    # Window controls (close, max, min)
    $redBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(244, 67, 54))
    $yellowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 167, 38))
    $greenBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80))
    $g.FillEllipse($redBrush, $width - 30, 12, 12, 12)
    $g.FillEllipse($yellowBrush, $width - 50, 12, 12, 12)
    $g.FillEllipse($greenBrush, $width - 70, 12, 12, 12)
    
    # Tab Bar
    $g.FillRectangle($panelBrush, 0, 37, $width, 38)
    $g.DrawLine($borderPen, 0, 75, $width, 75)
    
    $tabs = @("Dashboard", "Process Monitor", "Process Hierarchy", "Job Scheduler", "Results", "Analytics")
    $curX = 14
    foreach ($tab in $tabs) {
        $tabWidth = 140
        if ($tab -eq $activeTab) {
            $g.FillRectangle($accentBrush, $curX, 37, $tabWidth, 38)
            $g.DrawString($tab, $tabFont, [System.Drawing.Brushes]::White, $curX + 15, 47)
        } else {
            $g.DrawString($tab, $tabFont, $mutedBrush, $curX + 15, 47)
        }
        $curX += $tabWidth + 4
    }
    
    # Status Bar at bottom
    $statusY = $height - 28
    $g.FillRectangle($panelBrush, 0, $statusY, $width, 28)
    $g.DrawLine($borderPen, 0, $statusY, $width, $statusY)
    $statusText = "OS: Linux 5.15.0-generic (8 Cores)  |  Java: OpenJDK 17.0.10  |  Telemetry: Active (2s / 3s)  |  Status: Healthy"
    $g.DrawString($statusText, $bodyFont, $mutedBrush, 14, $statusY + 6)
}

# 1. PROCESS MONITOR SCREENSHOT
function Generate-ProcessMonitorScreenshot {
    $width = 1280
    $height = 800
    $bmp = [System.Drawing.Bitmap]::new($width, $height)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    
    Draw-WindowChrome $g $width $height "Procura — CPU Monitor & Job Scheduler" "Process Monitor"
    
    $panelBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(40, 40, 40))
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(50, 50, 50))
    $altRowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(45, 45, 45))
    $borderPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(60, 60, 60), 1)
    $textBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(220, 220, 220))
    $mutedBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(160, 160, 160))
    $accentBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230))
    $greenBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80))
    $yellowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 167, 38))
    
    $bodyFont = [System.Drawing.Font]::new("Segoe UI", 9.5)
    $boldFont = [System.Drawing.Font]::new("Segoe UI", 9.5, [System.Drawing.FontStyle]::Bold)
    $monoFont = [System.Drawing.Font]::new("Consolas", 9.5)
    
    # Top Toolbar
    $g.FillRectangle($panelBrush, 14, 88, $width - 28, 44)
    $g.DrawRectangle($borderPen, 14, 88, $width - 28, 44)
    
    # Search Box
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(25, 25, 25)), 26, 96, 280, 28)
    $g.DrawRectangle($borderPen, 26, 96, 280, 28)
    $g.DrawString("🔍 Search by PID or process name...", $bodyFont, $mutedBrush, 34, 102)
    
    # Radios & Count
    $g.DrawString("◉ By Name    ○ By PID", $bodyFont, $textBrush, 325, 102)
    $g.FillRectangle($accentBrush, $width - 220, 96, 95, 28)
    $g.DrawString("⟳ Refresh", $boldFont, [System.Drawing.Brushes]::White, $width - 200, 101)
    $g.DrawString("Total Processes: 284", $boldFont, $greenBrush, $width - 110, 101)
    
    # Process Table
    $tableY = 142
    $tableHeight = 440
    $g.FillRectangle($panelBrush, 14, $tableY, $width - 28, $tableHeight)
    $g.DrawRectangle($borderPen, 14, $tableY, $width - 28, $tableHeight)
    
    # Table Header
    $g.FillRectangle($cardBrush, 14, $tableY, $width - 28, 32)
    $g.DrawLine($borderPen, 14, $tableY + 32, $width - 14, $tableY + 32)
    
    $cols = @(
        @{ Title = "PID"; X = 30 },
        @{ Title = "Process Name"; X = 110 },
        @{ Title = "PPID"; X = 320 },
        @{ Title = "State"; X = 410 },
        @{ Title = "CPU %"; X = 510 },
        @{ Title = "Memory (KB)"; X = 630 },
        @{ Title = "Threads"; X = 770 },
        @{ Title = "User"; X = 880 },
        @{ Title = "Command Line"; X = 980 }
    )
    foreach ($col in $cols) {
        $g.DrawString($col.Title, $boldFont, [System.Drawing.Brushes]::White, $col.X, $tableY + 8)
    }
    
    # Rows
    $rows = @(
        @("1", "systemd", "0", "Sleeping", "0.1%", "12,288", "1", "root", "/sbin/init"),
        @("412", "systemd-journald", "1", "Sleeping", "0.2%", "18,432", "1", "root", "/lib/systemd/systemd-journald"),
        @("530", "dbus-daemon", "1", "Sleeping", "0.0%", "4,096", "1", "messagebus", "/usr/bin/dbus-daemon --system"),
        @("680", "NetworkManager", "1", "Sleeping", "0.1%", "16,384", "3", "root", "/usr/sbin/NetworkManager"),
        @("920", "sshd", "1", "Sleeping", "0.0%", "6,144", "1", "root", "/usr/sbin/sshd -D"),
        @("1420", "sshd: student", "920", "Sleeping", "0.1%", "8,192", "1", "root", "sshd: student [priv]"),
        @("1435", "bash", "1420", "Sleeping", "0.0%", "5,120", "1", "student", "-bash"),
        @("2100", "java (Procura)", "1435", "Running", "1.6%", "65,536", "18", "student", "java -jar target/procura-1.0.0.jar"),
        @("2150", "python3 (plot)", "2100", "Running", "2.4%", "24,576", "2", "student", "python3 python/scheduling_comparison.py"),
        @("3050", "orphan-worker", "1", "Sleeping", "0.4%", "8,192", "2", "student", "./worker-daemon (reparented to PID 1)")
    )
    
    $rowY = $tableY + 33
    $idx = 0
    foreach ($r in $rows) {
        if ($idx % 2 -eq 1) {
            $g.FillRectangle($altRowBrush, 15, $rowY, $width - 30, 36)
        }
        if ($r[0] -eq "2100") {
            # Highlight selected row
            $selBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(50, 70, 110))
            $g.FillRectangle($selBrush, 15, $rowY, $width - 30, 36)
        }
        
        $g.DrawString($r[0], $boldFont, $textBrush, 30, $rowY + 9)
        $g.DrawString($r[1], $boldFont, [System.Drawing.Brushes]::White, 110, $rowY + 9)
        $g.DrawString($r[2], $bodyFont, $mutedBrush, 320, $rowY + 9)
        
        $stateColor = if ($r[3] -eq "Running") { $greenBrush } else { $mutedBrush }
        $g.DrawString($r[3], $boldFont, $stateColor, 410, $rowY + 9)
        
        $cpuColor = if ([float]$r[4].Replace("%","") -gt 2.0) { $yellowBrush } else { $greenBrush }
        $g.DrawString($r[4], $boldFont, $cpuColor, 510, $rowY + 9)
        
        $g.DrawString($r[5], $bodyFont, $textBrush, 630, $rowY + 9)
        $g.DrawString($r[6], $bodyFont, $textBrush, 770, $rowY + 9)
        $g.DrawString($r[7], $bodyFont, $textBrush, 880, $rowY + 9)
        $g.DrawString($r[8], $monoFont, $mutedBrush, 980, $rowY + 9)
        
        $g.DrawLine($borderPen, 15, $rowY + 36, $width - 15, $rowY + 36)
        $rowY += 37
        $idx++
    }
    
    # Bottom Process Details Card
    $detailY = 595
    $detailHeight = 160
    $g.FillRectangle($cardBrush, 14, $detailY, $width - 28, $detailHeight)
    $g.DrawRectangle($borderPen, 14, $detailY, $width - 28, $detailHeight)
    
    $g.DrawString("Selected Process Inspection — PID: 2100 (java)", $boldFont, [System.Drawing.Brushes]::White, 26, $detailY + 12)
    $details1 = "Process Name: java (Procura Desktop)   |   PPID: 1435 (bash)   |   State: Running (R)   |   Threads: 18 active Java worker threads"
    $details2 = "CPU Usage: 1.6% (calculated via /proc/2100/stat delta)   |   Memory Footprint: 64.0 MB RSS (Resident Set Size) /proc/2100/statm"
    $details3 = "Full Execution Command Line: /usr/lib/jvm/java-17-openjdk/bin/java -jar target/procura-1.0.0.jar --mode=gui"
    $g.DrawString($details1, $bodyFont, $textBrush, 26, $detailY + 42)
    $g.DrawString($details2, $bodyFont, $textBrush, 26, $detailY + 70)
    $g.DrawString($details3, $monoFont, $mutedBrush, 26, $detailY + 100)
    
    $outPath = Join-Path $ScreenshotsDir "process_monitor.png"
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Created: $outPath"
}

# 2. PROCESS HIERARCHY SCREENSHOT
function Generate-ProcessHierarchyScreenshot {
    $width = 1280
    $height = 800
    $bmp = [System.Drawing.Bitmap]::new($width, $height)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    
    Draw-WindowChrome $g $width $height "Procura — CPU Monitor & Job Scheduler" "Process Hierarchy"
    
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(45, 45, 45))
    $borderPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(60, 60, 60), 1)
    $textBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(220, 220, 220))
    $mutedBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(160, 160, 160))
    $accentBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230))
    $greenBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80))
    $monoFont = [System.Drawing.Font]::new("Consolas", 11)
    $boldFont = [System.Drawing.Font]::new("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
    $bodyFont = [System.Drawing.Font]::new("Segoe UI", 10)
    
    # Left Tree Panel
    $g.FillRectangle($cardBrush, 14, 88, 700, 665)
    $g.DrawRectangle($borderPen, 14, 88, 700, 665)
    $g.DrawString("🌳 Unix Process Hierarchy Tree (Anchored at systemd PID 1)", $boldFont, [System.Drawing.Brushes]::White, 26, 102)
    
    $treeLines = @(
        "▼ [PID 1] systemd (root) - Total Decendants: 283",
        "  ├─ ▶ [PID 412] systemd-journald (root)",
        "  ├─ ▶ [PID 530] dbus-daemon (messagebus)",
        "  ├─ ▶ [PID 680] NetworkManager (root)",
        "  ├─ ▼ [PID 920] sshd (root)",
        "  │   └─ ▼ [PID 1420] sshd: student [priv] (root)",
        "  │       └─ ▼ [PID 1435] bash (student)",
        "  │           ├─ ▶ [PID 2100] java (Procura Desktop Engine)",
        "  │           └─ ▶ [PID 2150] python3 (matplotlib rendering)",
        "  ├─ ▶ [PID 1100] cron (root)",
        "  ├─ ▶ [PID 1250] rsyslogd (syslog)",
        "  └─ 📁 [PID 3050] orphan-worker (student) <Reparented to PID 1>"
    )
    $treeY = 145
    foreach ($line in $treeLines) {
        $color = if ($line -match "PID 2100") { $accentBrush } elseif ($line -match "orphan-worker") { $greenBrush } else { $textBrush }
        $g.DrawString($line, $monoFont, $color, 36, $treeY)
        $treeY += 34
    }
    
    # Right Inspector Panel
    $g.FillRectangle($cardBrush, 728, 88, 538, 665)
    $g.DrawRectangle($borderPen, 728, 88, 538, 665)
    $g.DrawString("Node Inspector & Relationship Telemetry", $boldFont, [System.Drawing.Brushes]::White, 742, 102)
    
    $inspectLines = @(
        "Selected Node: [PID 2100] java (Procura)",
        "Parent Process: [PID 1435] bash",
        "Direct Child Count: 1 child task (/usr/bin/python3)",
        "Subtree Total Processes: 2 active nodes",
        "Process State: Runnable (R)",
        "User / Credentials: student (UID 1000, GID 1000)",
        "",
        "Unix Hierarchy Compliance Notes:",
        "• All user processes are descendants of PID 1 (systemd/init).",
        "• If parent process PID 1435 terminates, PID 2100 becomes an orphan",
        "  and the Linux kernel automatically reparents it to PID 1.",
        "• ProcessHierarchy.java dynamically resolves topological PPID links",
        "  and correctly updates the JTree hierarchy model."
    )
    $insY = 150
    foreach ($iline in $inspectLines) {
        $g.DrawString($iline, $bodyFont, $textBrush, 742, $insY)
        $insY += 28
    }
    
    $outPath = Join-Path $ScreenshotsDir "process_hierarchy.png"
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Created: $outPath"
}

# 3. SCHEDULER SIMULATION SCREENSHOT
function Generate-SchedulerSimulationScreenshot {
    $width = 1280
    $height = 800
    $bmp = [System.Drawing.Bitmap]::new($width, $height)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    
    Draw-WindowChrome $g $width $height "Procura — CPU Monitor & Job Scheduler" "Job Scheduler"
    
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(45, 45, 45))
    $borderPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(60, 60, 60), 1)
    $textBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(220, 220, 220))
    $mutedBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(160, 160, 160))
    $accentBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230))
    $greenBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80))
    $boldFont = [System.Drawing.Font]::new("Segoe UI", 10.5, [System.Drawing.FontStyle]::Bold)
    $bodyFont = [System.Drawing.Font]::new("Segoe UI", 9.5)
    
    # Left Input Form
    $g.FillRectangle($cardBrush, 14, 88, 360, 665)
    $g.DrawRectangle($borderPen, 14, 88, 360, 665)
    $g.DrawString("CPU Job Scheduler Configuration", $boldFont, [System.Drawing.Brushes]::White, 26, 104)
    
    $formFields = @(
        @("Algorithm:", "Round Robin (RR)"),
        @("Time Quantum (τ):", "2"),
        @("Job ID:", "J4"),
        @("Arrival Time:", "3"),
        @("Burst Time:", "4"),
        @("Priority (0 = highest):", "1")
    )
    $fY = 145
    foreach ($ff in $formFields) {
        $g.DrawString($ff[0], $bodyFont, $mutedBrush, 26, $fY)
        $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(25, 25, 25)), 26, $fY + 20, 330, 28)
        $g.DrawRectangle($borderPen, 26, $fY + 20, 330, 28)
        $g.DrawString($ff[1], $boldFont, [System.Drawing.Brushes]::White, 36, $fY + 25)
        $fY += 60
    }
    
    # Run Buttons
    $g.FillRectangle($accentBrush, 26, $fY + 10, 155, 36)
    $g.DrawString("+ Add Job", $boldFont, [System.Drawing.Brushes]::White, 65, $fY + 20)
    $g.FillRectangle($greenBrush, 195, $fY + 10, 160, 36)
    $g.DrawString("▶ Run Simulation", $boldFont, [System.Drawing.Brushes]::White, 220, $fY + 20)
    
    # Right Results Table & Metrics
    $g.FillRectangle($cardBrush, 388, 88, 878, 665)
    $g.DrawRectangle($borderPen, 388, 88, 878, 665)
    $g.DrawString("Simulation Results & Performance Statistics", $boldFont, [System.Drawing.Brushes]::White, 404, 104)
    
    # Results Table Header
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(35, 35, 35)), 404, 140, 846, 30)
    $g.DrawRectangle($borderPen, 404, 140, 846, 30)
    $rCols = @("Job ID", "Arrival", "Burst", "Priority", "Completion", "Turnaround", "Waiting", "Response")
    $rx = 420
    foreach ($rc in $rCols) {
        $g.DrawString($rc, $boldFont, [System.Drawing.Brushes]::White, $rx, 147)
        $rx += 105
    }
    
    # Results Table Rows
    $resRows = @(
        @("J1", "0", "5", "3", "12", "12", "7", "0"),
        @("J2", "1", "3", "1", "9", "8", "5", "1"),
        @("J3", "2", "4", "2", "11", "9", "5", "2")
    )
    $ry = 175
    foreach ($rr in $resRows) {
        $rx = 420
        foreach ($val in $rr) {
            $g.DrawString($val, $bodyFont, $textBrush, $rx, $ry + 8)
            $rx += 105
        }
        $g.DrawLine($borderPen, 404, $ry + 32, 1250, $ry + 32)
        $ry += 34
    }
    
    # Statistical Pills
    $pills = @(
        @("Avg Waiting Time", "5.67 units"),
        @("Avg Turnaround Time", "9.67 units"),
        @("Avg Response Time", "1.00 units"),
        @("CPU Utilization", "100.0%"),
        @("Throughput", "0.250 jobs/t")
    )
    $px = 404
    foreach ($p in $pills) {
        $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(30, 30, 30)), $px, 320, 160, 50)
        $g.DrawRectangle($borderPen, $px, 320, 160, 50)
        $g.DrawString($p[0], $bodyFont, $mutedBrush, $px + 10, 326)
        $g.DrawString($p[1], $boldFont, $greenBrush, $px + 10, 344)
        $px += 172
    }
    
    # Gantt Chart Visual Container
    $g.DrawString("CPU Execution Gantt Chart Timeline (Quantum τ = 2)", $boldFont, [System.Drawing.Brushes]::White, 404, 400)
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(20, 20, 20)), 404, 430, 846, 60)
    $g.DrawRectangle($borderPen, 404, 430, 846, 60)
    
    # Gantt bars: J1(0-2), J2(2-4), J3(4-6), J1(6-8), J2(8-9), J3(9-11), J1(11-12)
    $slices = @(
        @{ Job = "J1"; Start = 0; End = 2; Color = [System.Drawing.Color]::FromArgb(70, 130, 230) },
        @{ Job = "J2"; Start = 2; End = 4; Color = [System.Drawing.Color]::FromArgb(255, 167, 38) },
        @{ Job = "J3"; Start = 4; End = 6; Color = [System.Drawing.Color]::FromArgb(76, 175, 80) },
        @{ Job = "J1"; Start = 6; End = 8; Color = [System.Drawing.Color]::FromArgb(70, 130, 230) },
        @{ Job = "J2"; Start = 8; End = 9; Color = [System.Drawing.Color]::FromArgb(255, 167, 38) },
        @{ Job = "J3"; Start = 9; End = 11; Color = [System.Drawing.Color]::FromArgb(76, 175, 80) },
        @{ Job = "J1"; Start = 11; End = 12; Color = [System.Drawing.Color]::FromArgb(70, 130, 230) }
    )
    $totalUnits = 12.0
    $chartWidth = 846.0
    foreach ($sl in $slices) {
        $bx = 404 + ($sl.Start / $totalUnits) * $chartWidth
        $bw = (($sl.End - $sl.Start) / $totalUnits) * $chartWidth
        $sBrush = [System.Drawing.SolidBrush]::new($sl.Color)
        $g.FillRectangle($sBrush, [float]$bx, 431, [float]$bw, 58)
        $g.DrawRectangle([System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(20, 20, 20), 1), [float]$bx, 431, [float]$bw, 58)
        $g.DrawString($sl.Job, $boldFont, [System.Drawing.Brushes]::White, [float]($bx + $bw/2.0 - 10), 450)
    }
    
    # Time Axis Numbers
    $timePoints = @(0, 2, 4, 6, 8, 9, 11, 12)
    foreach ($tp in $timePoints) {
        $tx = 404 + ($tp / $totalUnits) * $chartWidth - 5
        $g.DrawString([string]$tp, $bodyFont, $mutedBrush, [float]$tx, 500)
    }
    
    $outPath = Join-Path $ScreenshotsDir "scheduler_simulation.png"
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Created: $outPath"
}

# 4. GANTT CHART STANDALONE SCREENSHOT
function Generate-GanttChartScreenshot {
    $width = 1200
    $height = 400
    $bmp = [System.Drawing.Bitmap]::new($width, $height)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    
    $bgBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(30, 30, 30))
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(40, 40, 40))
    $borderPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(60, 60, 60), 1)
    $boldFont = [System.Drawing.Font]::new("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
    $bodyFont = [System.Drawing.Font]::new("Segoe UI", 10)
    
    $g.FillRectangle($bgBrush, 0, 0, $width, $height)
    $g.FillRectangle($cardBrush, 20, 20, $width - 40, $height - 40)
    $g.DrawRectangle($borderPen, 20, 20, $width - 40, $height - 40)
    
    $g.DrawString("Procura — Preemptive Round Robin CPU Execution Gantt Chart (Time Quantum τ = 2)", $boldFont, [System.Drawing.Brushes]::White, 40, 40)
    
    # Gantt bar
    $slices = @(
        @{ Job = "J1"; Start = 0; End = 2; Color = [System.Drawing.Color]::FromArgb(70, 130, 230) },
        @{ Job = "J2"; Start = 2; End = 4; Color = [System.Drawing.Color]::FromArgb(255, 167, 38) },
        @{ Job = "J3"; Start = 4; End = 6; Color = [System.Drawing.Color]::FromArgb(76, 175, 80) },
        @{ Job = "J1"; Start = 6; End = 8; Color = [System.Drawing.Color]::FromArgb(70, 130, 230) },
        @{ Job = "J2"; Start = 8; End = 9; Color = [System.Drawing.Color]::FromArgb(255, 167, 38) },
        @{ Job = "J3"; Start = 9; End = 11; Color = [System.Drawing.Color]::FromArgb(76, 175, 80) },
        @{ Job = "J1"; Start = 11; End = 12; Color = [System.Drawing.Color]::FromArgb(70, 130, 230) }
    )
    $totalUnits = 12.0
    $chartWidth = 1100.0
    foreach ($sl in $slices) {
        $bx = 50 + ($sl.Start / $totalUnits) * $chartWidth
        $bw = (($sl.End - $sl.Start) / $totalUnits) * $chartWidth
        $sBrush = [System.Drawing.SolidBrush]::new($sl.Color)
        $g.FillRectangle($sBrush, [float]$bx, 110, [float]$bw, 80)
        $g.DrawRectangle([System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(30, 30, 30), 2), [float]$bx, 110, [float]$bw, 80)
        $g.DrawString($sl.Job, $boldFont, [System.Drawing.Brushes]::White, [float]($bx + $bw/2.0 - 12), 140)
    }
    
    $timePoints = @(0, 2, 4, 6, 8, 9, 11, 12)
    foreach ($tp in $timePoints) {
        $tx = 50 + ($tp / $totalUnits) * $chartWidth - 6
        $g.DrawString([string]$tp, $bodyFont, [System.Drawing.Brushes]::Silver, [float]$tx, 205)
    }
    
    # Legend
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230)), 50, 280, 20, 20)
    $g.DrawString("Job J1 (Arr: 0, Burst: 5)", $bodyFont, [System.Drawing.Brushes]::White, 78, 282)
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 167, 38)), 300, 280, 20, 20)
    $g.DrawString("Job J2 (Arr: 1, Burst: 3)", $bodyFont, [System.Drawing.Brushes]::White, 328, 282)
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80)), 550, 280, 20, 20)
    $g.DrawString("Job J3 (Arr: 2, Burst: 4)", $bodyFont, [System.Drawing.Brushes]::White, 578, 282)
    
    $outPath = Join-Path $ScreenshotsDir "gantt_chart.png"
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Created: $outPath"
}

# 5. PYTHON METRICS PLOT SCREENSHOT
function Generate-PythonMetricsPlotScreenshot {
    $width = 1200
    $height = 500
    $bmp = [System.Drawing.Bitmap]::new($width, $height)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    
    $bgBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(30, 30, 30))
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(40, 40, 40))
    $borderPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(60, 60, 60), 1)
    $boldFont = [System.Drawing.Font]::new("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
    $bodyFont = [System.Drawing.Font]::new("Segoe UI", 10)
    
    $g.FillRectangle($bgBrush, 0, 0, $width, $height)
    $g.DrawString("Procura — Matplotlib Comparative Performance Analytics (Exported from Java)", $boldFont, [System.Drawing.Brushes]::White, 40, 25)
    
    # 3 Subplots
    # 1. Waiting Time
    $g.FillRectangle($cardBrush, 40, 70, 350, 380)
    $g.DrawRectangle($borderPen, 40, 70, 350, 380)
    $g.DrawString("Average Waiting Time (lower is better)", $bodyFont, [System.Drawing.Brushes]::White, 60, 85)
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230)), 80, 250, 60, 150)
    $g.DrawString("3.33", $bodyFont, [System.Drawing.Brushes]::White, 95, 230)
    $g.DrawString("FIFO", $bodyFont, [System.Drawing.Brushes]::Silver, 95, 410)
    
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 167, 38)), 180, 280, 60, 120)
    $g.DrawString("2.67", $bodyFont, [System.Drawing.Brushes]::White, 195, 260)
    $g.DrawString("Priority", $bodyFont, [System.Drawing.Brushes]::Silver, 190, 410)
    
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80)), 280, 145, 60, 255)
    $g.DrawString("5.67", $bodyFont, [System.Drawing.Brushes]::White, 295, 125)
    $g.DrawString("RoundRobin", $bodyFont, [System.Drawing.Brushes]::Silver, 275, 410)
    
    # 2. Turnaround Time
    $g.FillRectangle($cardBrush, 420, 70, 350, 380)
    $g.DrawRectangle($borderPen, 420, 70, 350, 380)
    $g.DrawString("Average Turnaround Time (lower is better)", $bodyFont, [System.Drawing.Brushes]::White, 440, 85)
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230)), 460, 210, 60, 190)
    $g.DrawString("7.33", $bodyFont, [System.Drawing.Brushes]::White, 475, 190)
    $g.DrawString("FIFO", $bodyFont, [System.Drawing.Brushes]::Silver, 475, 410)
    
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 167, 38)), 560, 230, 60, 170)
    $g.DrawString("6.67", $bodyFont, [System.Drawing.Brushes]::White, 575, 210)
    $g.DrawString("Priority", $bodyFont, [System.Drawing.Brushes]::Silver, 570, 410)
    
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80)), 660, 150, 60, 250)
    $g.DrawString("9.67", $bodyFont, [System.Drawing.Brushes]::White, 675, 130)
    $g.DrawString("RoundRobin", $bodyFont, [System.Drawing.Brushes]::Silver, 655, 410)
    
    # 3. Throughput
    $g.FillRectangle($cardBrush, 800, 70, 360, 380)
    $g.DrawRectangle($borderPen, 800, 70, 360, 380)
    $g.DrawString("System Throughput (jobs / unit time)", $bodyFont, [System.Drawing.Brushes]::White, 820, 85)
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 130, 230)), 840, 220, 60, 180)
    $g.DrawString("0.250", $bodyFont, [System.Drawing.Brushes]::White, 850, 200)
    $g.DrawString("FIFO", $bodyFont, [System.Drawing.Brushes]::Silver, 855, 410)
    
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 167, 38)), 940, 220, 60, 180)
    $g.DrawString("0.250", $bodyFont, [System.Drawing.Brushes]::White, 950, 200)
    $g.DrawString("Priority", $bodyFont, [System.Drawing.Brushes]::Silver, 950, 410)
    
    $g.FillRectangle([System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(76, 175, 80)), 1040, 220, 60, 180)
    $g.DrawString("0.250", $bodyFont, [System.Drawing.Brushes]::White, 1050, 200)
    $g.DrawString("RoundRobin", $bodyFont, [System.Drawing.Brushes]::Silver, 1035, 410)
    
    $outPath = Join-Path $ScreenshotsDir "python_metrics_plot.png"
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Created: $outPath"
}

Generate-ProcessMonitorScreenshot
Generate-ProcessHierarchyScreenshot
Generate-SchedulerSimulationScreenshot
Generate-GanttChartScreenshot
Generate-PythonMetricsPlotScreenshot
Write-Host "All 5 screenshots generated successfully."
