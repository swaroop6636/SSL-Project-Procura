# AI Usage Declaration – Procura Project

## Project Information

- **Project Title:** Procura – CPU Monitor and Job Scheduler
- **Academic Course:** Operating Systems Laboratory / BTech Capstone Project
- **Academic Year:** 2025–2026
- **Institution:** Department of Computer Science and Engineering

---

## 1. Overview and Purpose

This document explains how we used AI tools while working on our Procura project. During the development process, we used tools such as ChatGPT, Claude and Gemini to get help with coding, understanding concepts, solving errors and preparing project documentation.

We mainly used AI as a supporting tool whenever we needed guidance or faced difficulties during development. It helped us with initial code structures, design ideas and documentation. However, we did not directly depend on AI-generated content for the entire project. We reviewed the code, made changes wherever required and tested the different parts of the application ourselves.

As a team, we are responsible for understanding the project and making sure that the final implementation works as expected.

---

## 2. Areas Where AI Was Used

We used AI assistance in a few parts of the project during development.

### 2.1 Help with Coding

- **Basic Code Structures:** We used AI to get initial code examples for Java Swing components, including layouts, tables and button actions.
- **File Parsing:** We referred to AI suggestions for reading and extracting information from Linux `/proc/[pid]/stat` and `/proc/meminfo` files.
- **Python Scripts:** We used AI for basic guidance while writing Python scripts to generate graphs using Matplotlib from CSV files.

### 2.2 Design and Implementation Guidance

- **Project Structure:** We used AI suggestions to understand how to organise the scheduling algorithms separately from the GUI code. We also explored the Strategy design pattern for this purpose.
- **Thread Management:** We referred to AI for understanding how background monitoring tasks can work without affecting the Java Swing interface. This included the use of `ScheduledExecutorService` and `SwingUtilities.invokeLater`.

### 2.3 Documentation

- We used AI to get help with the initial structure of the project documentation and reports.
- It also helped us with Markdown formatting, LaTeX templates and BibTeX references.
- We used it to improve sentence formation and make the documentation easier to read.

---

## 3. Project Components Where AI Assistance Was Used

The following table explains where we took AI assistance and what changes and work we carried out ourselves.

| Component                     | How AI Helped                                                                      | Our Work                                                                                                                            |
| ----------------------------- | ---------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| **ProcessMonitor.java**       | Provided initial suggestions for reading Linux process information.                | We worked on parsing the required values, calculating CPU usage using time differences and handling permission-related errors.      |
| **ProcessHierarchy.java**     | Provided examples for creating a process tree using `DefaultMutableTreeNode`.      | We implemented the process hierarchy using PID and PPID values and handled processes that did not have a matching parent.           |
| **CPU Scheduling Algorithms** | Helped us understand the basic logic of FIFO, Priority and Round Robin scheduling. | We implemented and tested the scheduling algorithms, including process execution, preemption, idle CPU time and scheduling metrics. |
| **Java Swing Interface**      | Provided basic examples for layouts and GUI components.                            | We modified the interface, added tables, buttons, sorting options and status information to make the application easier to use.     |
| **Python Visualization**      | Provided basic examples of Matplotlib graphs.                                      | We worked on reading CSV data and generating scheduling graphs and Gantt charts using Python.                                       |
| **LaTeX Reports and Slides**  | Helped with basic LaTeX structure and formatting.                                  | We prepared the project content, algorithm explanations, reports and presentation slides.                                           |

---

## 4. Our Responsibilities and Verification

Throughout the project, we made sure to review and test the work before including it in the final application.

The following are some of the steps we followed:

1. **Understanding the Code:** We went through the different classes and methods used in the project so that we could understand how each part works and explain it during the project evaluation.

2. **Checking Scheduling Results:** We manually calculated completion time, turnaround time, waiting time and response time for different processes. We compared these results with the output of our simulator to check whether the calculations were correct.

3. **Testing on Linux:** We built and ran the application on Linux systems, including Ubuntu 22.04 LTS and Debian 12. We checked whether the process information was being collected correctly and fixed issues that appeared during testing.

4. **Fixing Errors:** During development, we faced some issues related to thread handling, GUI updates and scheduling edge cases. We worked on fixing these issues and made changes to the initial code wherever needed.

---

## 5. Testing and Validation

We performed different types of testing to check whether the main features of Procura were working properly.

### Automated Testing (JUnit 5)

We used JUnit 5 to test the scheduling algorithms and other important parts of the application.

Some of the cases we tested include:

- FIFO scheduling with different process arrival times and CPU idle periods.
- Priority scheduling with different priorities and processes having the same priority.
- Round Robin scheduling with different time quantum values, including 1, 2 and 4.
- Checking how processes enter and re-enter the ready queue.
- Testing the parsing of process information using sample `/proc` data.

### Manual Testing

Apart from automated testing, we also checked some results manually.

- We compared Round Robin scheduling results with Gantt chart examples to check the execution order.
- We compared the process information shown in Procura with Linux commands such as `top` and `ps -aux`.
- We manually checked scheduling calculations using different input values.

### Performance and Thread Testing

We used JDK Flight Recorder (JFR) to observe the application's performance and check how background monitoring tasks were running.

We also checked that the monitoring process did not make the GUI unresponsive during execution.

---

## 6. Individual Team Member Contributions and Declarations

The project work was divided among three members based on the different parts of the application. Each member was responsible for their assigned work and tested the features they worked on.

### Member 1: Swaroop / CS25BT006

- **Assigned Area:** Process Monitoring and CPU/Memory Data Collection
- **Contribution:** I worked on collecting process information from Linux `/proc` files, mainly `/proc/[pid]/stat` and `/proc/meminfo`. I also worked on calculating CPU usage using time differences and handling memory-related values.
- **Verification:** I tested the process and memory information on Linux and compared the results with system commands to check whether the values were correct.



### Member 2: Bunny / CS25BT016

- **Assigned Area:** Process Management, CPU Scheduling and Concurrency
- **Contribution:** I worked on building the process hierarchy and implementing the FIFO, Priority and Round Robin scheduling algorithms. I also worked on background monitoring tasks, thread management and calculating scheduling performance metrics.
- **Verification:** I tested the scheduling algorithms with different inputs and checked the waiting time, turnaround time and response time. I also checked that background tasks were running without affecting the GUI.



### Member 3: Basha / CS25BT059

- **Assigned Area:** GUI Development, Visualization, Testing and Documentation
- **Contribution:** I worked on developing the Java Swing interface, including the panels, tables and data display. I also worked on CSV export, Python graph generation using Matplotlib, JUnit testing, build scripts, performance profiling and project documentation.
- **Verification:** I tested the main features of the application, checked CSV export and graph generation, ran the test cases and made sure that the project was building and running properly.



---

## 7. Academic Integrity Statement

We declare that Procura was developed as part of our academic project work and that we take responsibility for the work submitted.

We also confirm the following:

1. The project was developed through our team's own efforts and understanding.
2. AI tools were used only for guidance, coding assistance, understanding concepts and preparing documentation.
3. We reviewed and tested the code used in the project and made changes wherever necessary.
4. All team members are responsible for understanding their assigned parts of the project and explaining them during evaluation.
5. External references, textbooks and official documentation used during development have been acknowledged in the project report.
