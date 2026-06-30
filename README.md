Job Shop Scheduling System

A concurrent Java application implementing a thread safe job shop scheduler using ReentrantLock and Condition variables.

Overview

Simulates a manufacturing system where jobs are scheduled across multiple machines using First-Come-First-Served (FCFS) scheduling. Demonstrates advanced Java concurrency patterns for real-time resource management.

Course: F29OC (Object-Oriented & Concurrent Programming) | Heriot-Watt University Dubai

Technologies

Java 21 | Concurrency: ReentrantLock, Condition Variables | Testing: Custom Test Suite

Quick Start

Compile

bashcd src
javac *.java

Run

bashjava App

Key Features

✅ Thread-safe job scheduling with locks and conditions

✅ Multi-machine resource management

✅ FCFS scheduling algorithm

✅ Comprehensive test suite


Project Structure

src/

├── JobShopManager.java    # Core scheduling logic

├── App.java               # Entry point

├── Tests.java             # Test cases

├── Job.java               # Data model (read-only)

├── Operation.java         # Data model (read-only)

└── JobShopInterface.java  # Interface (read-only)

Implementation Notes


JobShopManager uses only ReentrantLock and Condition for synchronization
Implements thread-safe machine and job management
No exceptions thrown - handled internally
Tests verify FCFS scheduling and machine allocation


