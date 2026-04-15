---
name: marslib-threading
description: Dictates the elite standard for writing custom java Thread instances in MARSLib, strictly enforcing time-unit sleeping and interrupt safeties to prevent loop lockups.
---

# MARSLib Threading Standards

Any time you are tasked with creating a background loop (e.g., `PhoenixOdometryThread`, a Custom Coprocessor Listener, or I2C buffer queues) it must be perfectly thread-safe and adhere to MARSLib's `ProjectDoctor` linting standards.

## 1. No Infinite Loops
NEVER write `while (true)` inside a `run()` loop. The JVM handles thread interruption automatically during RoboRIO code restarts or autonomous transitions.
**Always use:**
```java
@Override
public void run() {
    while (!Thread.currentThread().isInterrupted()) {
        // ... Logic ...
    }
}
```

## 2. No Raw Thread Sleeps
ProjectDoctor explicitly flags `Thread.sleep(...)` as a dangerous blocking call because it's ambiguous what unit of time is being requested, often leading to 20ms delays becoming 20 second loop lockups.

**Always use `java.util.concurrent.TimeUnit`:**
```java
try {
    TimeUnit.MILLISECONDS.sleep(20);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Restore interrupt status
    break; // Safely exit the loop
}
```
Never swallow the `InterruptedException`. You must properly re-assert the interrupt state to gracefully shut down the thread pool.
