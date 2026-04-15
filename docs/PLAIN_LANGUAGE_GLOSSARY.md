# MARSLib Plain Language Glossary

This guide explains technical robotics terms in simple language. Use these explanations when writing documentation or helping new team members.

## Core Concepts

### IO Abstraction Layer
**Technical**: A design pattern that decouples hardware-specific implementation details from high-level robot logic.

**Plain Language**: The IO Abstraction Layer separates your robot code from the physical hardware. Think of it like a universal remote - the same remote works with different TV brands. Your code talks to an "interface" (a contract), not to specific motors or sensors.

**Analogy**: When you use a smartphone, you don't need to know how the touch screen works internally. You just tap buttons and things happen. The IO layer is like that - your code says "move forward" and the hardware makes it happen, regardless of which motor brand you're using.

### Zero-Allocation
**Technical**: Avoiding memory allocations in time-critical code paths to prevent garbage collection pauses.

**Plain Language**: Zero-allocation means not creating new objects while the robot is running its control loops (every 20 milliseconds). If you create too many new objects, the Java Garbage Collector has to pause your robot to clean up memory, which can make your robot stutter.

**Analogy**: Think of memory allocation like cleaning your room. If you had to stop and clean every 20 seconds, you couldn't get anything done. Zero-allocation means keeping your room tidy as you go, so you never need to stop and clean.

### Odometry
**Technical**: The use of data from motion sensors to estimate change in position over time.

**Plain Language**: Odometry (oh-DOM-it-ree) is how the robot knows where it is on the field. It uses wheel sensors to track movement and calculates the robot's position.

**Analogy**: It's like counting steps to know how far you've walked. If you know your starting position and count every step you take, you can figure out where you are now. The robot does the same thing with wheel rotations.

### PID Controller
**Technical**: A control loop mechanism employing feedback to maintain a process at a desired setpoint.

**Plain Language**: A PID controller is a tool that helps the robot reach and maintain a target position or speed. It constantly checks the current state and makes adjustments.

**Analogy**: Think about a thermostat. It checks the current temperature, compares it to the desired temperature, and turns the heating or cooling on or off to reach the target. A PID controller does the same thing for robot movement - it checks where the robot is and adjusts the motors to get to the right position.

### CAN Bus
**Technical**: A vehicle bus standard designed to allow microcontrollers and devices to communicate with each other.

**Plain Language**: The CAN Bus is like a conversation where robot parts take turns speaking on a shared wire. Instead of each device having its own wires, they all share one communication line.

**Analogy**: Think of it like a classroom discussion. Only one person can speak at a time, or nobody would understand anything. The CAN Bus makes sure devices take turns talking so communication is clear and organized.

### Thread Safety
**Technical**: Ensuring that shared data is accessed safely when multiple threads execute concurrently.

**Plain Language**: Thread safety means making sure different parts of your program don't mess up each other's work when they're running at the same time.

**Analogy**: Imagine two people trying to write on the same piece of paper at the same time. Their writing would get mixed up and unreadable. Thread safety is like taking turns - one part of your program writes, then the other part writes, so nothing gets mixed up.

### State Machine
**Technical**: A mathematical model of computation that can be in one of a finite number of states at any given time.

**Plain Language**: A state machine is a way to organize robot behavior into different "states" or modes. The robot can only be in one state at a time, and it switches between states based on certain conditions.

**Analogy**: Think about a video game character. They might have states like "idle," "running," "jumping," or "attacking." The character can only be in one state at a time, and they switch between states when you press buttons. Robot state machines work the same way.

### Gyroscope
**Technical**: A device used for measuring or maintaining orientation and angular velocity.

**Plain Language**: A gyroscope (EYE-ro-scope) tells the robot which direction it's facing and how fast it's turning.

**Analogy**: It's like having a compass that also tells you when you're turning. If you spin around in a chair with your eyes closed, you know you're spinning because of how you feel. The gyroscope gives the robot that same sense.

### Encoders
**Technical**: Sensors that convert mechanical motion into electrical signals to measure position or speed.

**Plain Language**: Encoders are sensors that measure how far a wheel or motor has turned. They help the robot know how fast it's moving and how far it has traveled.

**Analogy**: Think of the odometer in a car - it counts how many miles you've driven. Robot encoders do the same thing but for wheel rotations instead of miles.

### Simulation
**Technical**: The process of modeling real-world systems with computer programs to predict behavior.

**Plain Language**: Simulation means creating a computer version of your robot to test code without using the real robot. It's like a video game version of your robot.

**Analogy**: Flight simulators let pilots practice flying without real planes. Robot simulators let programmers practice coding without real robots. If you crash in the simulator, nothing breaks!

### Garbage Collection
**Technical**: Automatic memory management that reclaims memory occupied by objects no longer in use.

**Plain Language**: Java automatically cleans up memory when objects are no longer needed. This cleaning process is called "garbage collection." Sometimes it pauses your program to do the cleaning.

**Analogy**: Think of it like a parent cleaning up toys while kids are playing. The kids have to pause playing while the parent picks up old toys. If this happens too often, it slows down playtime.

### Real-Time
**Technical**: Computing systems that respond to inputs within guaranteed time constraints.

**Plain Language**: Real-time means the robot must respond quickly and predictably. The robot can't delay or it won't work properly.

**Analogy**: Think about playing a musical instrument. You need to press the right notes at the right time, or the music sounds wrong. Robot code is the same - it needs to run at the right time or the robot won't work correctly.

### Duty Cycle
**Technical**: The fraction of one period in which a signal or system is active.

**Plain Language**: Duty cycle is how much time something is "on" versus "off." It's often used to control motor speed.

**Analogy**: Think of flicking a light switch on and off quickly. If the light is on for half the time and off for half the time, that's a 50% duty cycle. The light appears dimmer than if it were on all the time. Robot motors use this same idea to control speed.

### Deadband
**Technical**: A range of input values for which a system produces no output.

**Plain Language**: Deadband is a small range around zero where the robot ignores joystick input. This prevents the robot from moving when you're not touching the controller.

**Analogy**: Some joysticks are sensitive and might detect tiny movements even when you're not touching them. Deadband is like telling the robot, "If the joystick is barely moving, pretend it's not moving at all."

### Feedforward
**Technical**: A control strategy that anticipates required actions based on system model and target.

**Plain Language**: Feedforward means the robot makes a good guess about what to do based on what it wants to accomplish, rather than just reacting to errors.

**Analogy**: Imagine throwing a ball to a friend. You don't just throw it and then adjust - you aim based on where you want it to go. That's feedforward. You're planning ahead based on the goal.

### Feedback
**Technical**: Information about the output of a system used to guide future actions.

**Plain Language**: Feedback means the robot checks what it's actually doing and adjusts based on the difference between what it's doing and what it wants to do.

**Analogy**: When you're walking toward a door, you constantly check if you're going straight and make small adjustments. That's feedback. You're not just planning your path - you're constantly correcting based on what you see.

### Path Following
**Technical**: The control problem of making a vehicle follow a predetermined trajectory.

**Plain Language**: Path following means the robot follows a planned route on the field. The robot adjusts its steering and speed to stay on the path.

**Analogy**: Think about a train following tracks. The train doesn't decide where to go - it follows the tracks. Path following is like giving your robot invisible tracks to follow across the field.

### Holonomic Drive
**Technical**: A drive system that can move in any direction regardless of orientation.

**Plain Language**: Holonomic (hoh-low-NOM-ik) drive means the robot can move in any direction without turning first. It can move sideways, diagonally, or straight.

**Analogy**: Most cars can only move forward or backward (they're not holonomic). A shopping cart is holonomic - you can push it sideways, diagonally, or in any direction. Swerve drive robots are holonomic.

### Brownout
**Technical**: A drop in voltage supply that can cause electronic devices to reset or malfunction.

**Plain Language**: Brownout happens when the robot uses too much power at once, causing voltage to drop and the robot's computer to restart.

**Analogy**: Think about turning on too many appliances at home - sometimes the lights dim because there's not enough power. Robot brownouts are similar. If the robot tries to move too fast, the voltage drops and the computer restarts.

### Latency
**Technical**: The delay between input and response in a system.

**Plain Language**: Latency is the time delay between when something happens and when the robot responds to it.

**Analogy**: When you play video games online, sometimes there's a delay between when you press a button and when your character moves. That's latency. In robots, we want latency to be as small as possible so the robot responds quickly.

## Hardware Terms

### Talon FX / SPARK MAX
**Technical**: Motor controllers used in FRC robots.

**Plain Language**: These are devices that connect between the robot's computer and the motors. They take commands from the computer and send the right amount of power to the motors.

**Analogy**: Think of them like volume knobs for motors. The computer tells the motor controller how fast to spin, and the motor controller adjusts the power to make it happen.

### Pigeon2 / NavX
**Technical**: Inertial measurement units (IMUs) used in FRC.

**Plain Language**: These are sensors that tell the robot which way it's facing, how fast it's turning, and whether it's accelerating.

**Analogy**: They're like the robot's inner ear - just like you can tell when you're spinning, falling, or accelerating, these sensors give the robot that same information.

### Limelight / PhotonVision
**Technical**: Vision processing systems for FRC robots.

**Plain Language**: These are camera systems that help the robot see and track objects on the field, like game pieces or targets.

**Analogy**: Think of them like the robot's eyes. They take pictures and figure out where important objects are, like how far away a target is or which way to turn to face it.

## Software Terms

### Subsystem
**Technical**: A collection of hardware and software that performs a specific function.

**Plain Language**: A subsystem is a part of your robot that does one specific job, like driving, shooting, or climbing.

**Analogy**: Think of your body - you have a digestive system, a nervous system, and a muscular system. Each system has a specific job. Robot subsystems work the same way.

### Command
**Technical**: A stateless action that runs when scheduled and interacts with subsystems.

**Plain Language**: A command is a specific action for the robot to perform, like "move forward 2 meters" or "shoot the ball."

**Analogy**: Commands are like individual tasks on a to-do list. "Brush teeth" is one task, "make bed" is another. Robot commands are tasks the robot can do.

### Trigger
**Technical**: A condition that causes a command to run.

**Plain Language**: A trigger is something that starts a command, like pressing a button on the controller.

**Analogy**: Think of triggers like the buttons on a TV remote. When you press "volume up," that triggers the TV to increase volume. Robot triggers start robot actions.

## Using This Glossary

When writing documentation:
1. Use the technical term first
2. Follow with the plain language explanation
3. Include the analogy if it helps clarify

**Example**:
```
## Odometry

Odometry uses wheel sensors to track the robot's position. The robot counts 
wheel rotations to calculate where it is on the field.

**In other words**: It's like counting steps to know how far you've walked. 
If you know where you started and count every step, you can figure out where 
you are now.
```

---

*This glossary is part of MARSLib's commitment to making robotics accessible to everyone. For more readability guidelines, see our [Readability Guidelines](/docs/READABILITY_GUIDELINES.md).*