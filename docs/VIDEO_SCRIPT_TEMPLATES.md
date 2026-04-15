# Video Script Templates

Use these templates to create educational videos about MARSLib. Videos should be short, clear, and filmed at middle school level.

## Template 1: Concept Explainer (2-3 minutes)

### Purpose
Explain a single concept clearly for beginners

### Structure

```markdown
# Title: [Concept Name] Explained

## Opening (15 seconds)
- **Hook**: Start with a question or problem
- **Preview**: What you'll learn in this video
- **Duration**: "In this 2-minute video, you'll learn..."

## Section 1: What is [Concept]? (45 seconds)
- **Simple definition**: "Think of [concept] as..."
- **Analogy**: Compare to everyday thing
- **Why it matters**: How it helps your robot

## Section 2: How Does It Work? (45 seconds)
- **Basic explanation**: Step-by-step process
- **Visual demonstration**: Show it working
- **Key points**: 2-3 main takeaways

## Section 3: Code Example (30 seconds)
- **Show code**: Brief, focused example
- **Explain key lines**: Don't explain everything
- **What it does**: Brief explanation

## Summary (15 seconds)
- **Recap**: Main points
- **Next steps**: What to learn next
- **Call to action**: "Check the documentation for more info"
```

### Example: What is a Subsystem?

```markdown
# Title: What is a Subsystem? - MARSLib Explained

## Opening (15 seconds)
"Ever wonder how to organize your robot code? Subsystems are the answer! 
In this 2-minute video, you'll learn what subsystems are and how to use them."

## Section 1: What is a Subsystem? (45 seconds)
"A subsystem is like a department in a company. Just as a company has 
different departments for sales, engineering, and finance, your robot has 
different parts that do different jobs.

Think of it this way: your robot's drivetrain is one subsystem, your shooter 
is another, and your intake is a third. Each subsystem manages its own 
hardware and provides simple commands to control it."

## Section 2: How Does It Work? (45 seconds)
"Subsystems work by grouping related things together. A drivetrain subsystem 
manages the motors, encoders, and sensors for driving. It gives you simple 
methods like 'driveForward' or 'turn' instead of having to control each motor 
individually.

This makes your code cleaner and easier to understand. Instead of telling 
four motors what to do, you just tell the drivetrain 'drive forward 2 meters'."

## Section 3: Code Example (30 seconds)
"Here's how simple a subsystem looks:

```java
public class Drivetrain extends Subsystem {
    private final SwerveDrive drive;
    
    public void drive(double vx, double vy, double omega) {
        drive.drive(vx, vy, omega);
    }
}
```

Just three lines to create a drivetrain that can move in any direction!"

## Summary (15 seconds)
"So remember: subsystems organize your robot code by grouping related 
hardware together. They make your code cleaner, easier to understand, and 
simpler to maintain.

Want to learn more? Check out the subsystem guide in the MARSLib documentation!"
```

---

## Template 2: Tutorial/How-To (5-7 minutes)

### Purpose
Teach a specific skill or task step-by-step

### Structure

```markdown
# Title: How to [Task] with MARSLib

## Opening (30 seconds)
- **Problem**: What you'll accomplish
- **Prerequisites**: What you need to know first
- **Outcome**: What you'll have by the end
- **Time**: "This takes about X minutes..."

## Step 1: [First Step] (90 seconds)
- **What we're doing**: Explain the step
- **Show how**: Demonstrate it
- **Explain why**: Why this matters
- **Check for understanding**: "Make sure you see..."

## Step 2: [Second Step] (90 seconds)
[Follow same pattern]

## Step 3: [Third Step] (90 seconds)
[Follow same pattern]

## [Continue for all steps...]

## Testing (60 seconds)
- **How to test**: Verify it works
- **What to expect**: What should happen
- **Common issues**: What might go wrong

## Summary (30 seconds)
- **Recap**: What you learned
- **Troubleshooting**: Quick tips
- **Next steps**: What to learn next
```

### Example: How to Set Up a Swerve Drive

```markdown
# Title: How to Set Up a Swerve Drive - MARSLib Tutorial

## Opening (30 seconds)
"In this tutorial, you'll learn how to set up a swerve drive using MARSLib. 
By the end, you'll have a working swerve drive that you can control with a 
gamepad. This takes about 6 minutes."

## Step 1: Create Your Drivetrain Subsystem (90 seconds)
"First, we need to create a drivetrain subsystem. This manages all four 
swerve modules and handles the complex math of making them work together.

Let me show you the code..."

[Show code creation in VS Code]

"Notice we're using the SwerveDrive class from MARSLib. It handles all the 
hard stuff for us. We just need to tell it to drive when we want to move."

## Step 2: Create a Drive Command (90 seconds)
"Next, we create a command that actually drives the robot. This reads the 
gamepad and tells the drivetrain what to do.

The key here is that we're getting joystick values and passing them to the 
drivetrain. The drivetrain handles the rest."

[Show command code]

"See how simple that is? Just read the joystick and call drive()."

## Step 3: Set Up Your Robot Container (90 seconds)
"Now we need to tie everything together. The RobotContainer creates all 
our subsystems and commands.

Here we're setting the drive command as the default command, which means 
it runs automatically whenever no other command needs the drivetrain."

[Show RobotContainer setup]

## Testing (60 seconds)
"Let's test this in simulation. I'll run the code and show you how it works..."

[Run simulation, demonstrate driving]

"You can see the robot moving in response to my key presses. The same code 
will work on a real robot with no changes!"

## Summary (30 seconds)
"So in just a few steps, you've created a swerve drive! You created a 
drivetrain subsystem, a drive command, and tied them together in the 
robot container.

Next, you might want to learn about path following or autonomous driving. 
Check out the MARSLib documentation for more tutorials!"
```

---

## Template 3: Troubleshooting Guide (3-4 minutes)

### Purpose
Help viewers diagnose and fix specific problems

### Structure

```markdown
# Title: [Problem] - How to Fix It

## Opening (30 seconds)
- **Problem**: What's broken
- **Impact**: Why it matters
- **Duration**: "We'll fix this in X minutes"

## Quick Diagnosis (60 seconds)
- **What to check**: Quick things to verify first
- **How to check**: Show the diagnostic steps
- **What you're looking for**: Good vs bad results

## Common Causes (90 seconds)
- **Cause 1**: Explain + show fix
- **Cause 2**: Explain + show fix
- **Cause 3**: Explain + show fix

## Solution (60 seconds)
- **Step-by-step fix**: Show complete solution
- **Verify it works**: Test the fix
- **Prevent it happening again**: Tips

## Summary (30 seconds)
- **Recap**: What was wrong and how to fix it
- **Related issues**: Other problems to watch for
```

---

## Template 4: Quick Tips (30-60 seconds)

### Purpose
Share quick, useful tips

### Structure

```markdown
# Title: Quick Tip: [Topic]

## Opening (10 seconds)
- **Tip preview**: What you'll learn
- **Why it matters**: Brief benefit

## The Tip (30-40 seconds)
- **Show how**: Demonstrate the tip
- **Explain**: Why it works
- **Example**: Real use case

## Summary (10 seconds)
- **Recap**: One sentence summary
- **Learn more**: Link to full guide
```

### Example: Quick Tip - Zero Allocation

```markdown
# Title: Quick Tip: Avoid Memory Allocations in Periodic

## Opening (10 seconds)
"Here's a quick tip to keep your robot running smoothly: don't create new 
objects in your periodic methods!"

## The Tip (40 seconds)
"Creating new objects like 'new Pose2d()' in periodic methods causes Java 
to pause for garbage collection, making your robot stutter.

Instead, create objects once in your constructor and reuse them:

```java
// ✅ Good: Create once
private final Pose2d pose = new Pose2d();

public void periodic() {
    pose.setX(x);  // Reuse the object
}

// ❌ Bad: Creates new object every 20ms!
public void periodic() {
    Pose2d pose = new Pose2d(x, y);
}
```

Your robot will run much smoother!"

## Summary (10 seconds)
"Remember: pre-allocate objects and reuse them. Check out the zero allocation 
guide for more details!"
```

---

## Template 5: Challenge Walkthrough (4-5 minutes)

### Purpose
Show solution to a coding challenge

### Structure

```markdown
# Title: [Challenge Name] - Solution Walkthrough

## Opening (30 seconds)
- **Challenge**: What we're solving
- **Difficulty**: Who this is for
- **Prerequisites**: What you need to know

## Understanding the Problem (60 seconds)
- **Break down**: What the challenge asks
- **Think through**: Approach to solution
- **Plan**: Steps to implement

## Implementation (180 seconds)
- **Step 1**: Show first part of solution
- **Explain**: Why we do it this way
- **Test**: Show it working
- **Continue**: Show each subsequent step
- **Build**: Complete solution piece by piece

## Final Code (30 seconds)
- **Show complete**: Final working solution
- **Explain**: How it all works together

## Summary (30 seconds)
- **Recap**: What we built
- **Variations**: How to extend it
- **Next challenge**: What to try next
```

---

## Filming Guidelines

### Technical Setup

**Equipment**:
- Microphone: USB lapel mic or USB desk mic
- Camera: Webcam or phone camera
- Lighting: Ring light or natural light
- Background: Clean, uncluttered

**Software**:
- Recording: OBS Studio, Loom, or built-in screen recorder
- Editing: DaVinci Resolve (free) or similar
- Code display: VS Code with good font size

### Presentation Tips

**Speaking**:
- ✅ Speak clearly and at moderate pace
- ✅ Use everyday language
- ✅ Explain technical terms
- ✅ Pause for emphasis
- ❌ Don't rush through complex parts

**Screen Recording**:
- ✅ Use large font size (18pt+)
- ✅ Highlight important code sections
- ✅ Show full context, not just snippets
- ✅ Move mouse smoothly
- ❌ Don't scroll too fast

**Structure**:
- ✅ Start with why, not what
- ✅ Show before telling
- ✅ Use analogies for complex concepts
- ✅ Include "In other words" sections
- ✅ End with clear next steps

### Accessibility

**Captions**:
- Add subtitles to all videos
- Use proper capitalization and punctuation
- Include sound descriptions [clicks, typing, etc.]

**Visuals**:
- High contrast for text
- Large, readable fonts
- Clear diagrams and illustrations
- Consistent color scheme

**Content**:
- Define technical terms
- Use analogies and examples
- Include transcripts
- Provide multiple explanations

---

## Video Quality Checklist

### Before Recording
- [ ] Script written at middle school level
- [ ] Examples tested and working
- [ ] Screen recording software configured
- [ ] Microphone tested
- [ ] Lighting adequate

### During Recording
- [ ] Speak clearly and slowly
- [ ] Explain what you're doing
- [ ] Show complete context
- [ ] Highlight important parts
- [ ] Test code runs successfully

### After Recording
- [ ] Add captions/subtitles
- [ ] Edit for clarity and pacing
- [ ] Include transcript
- [ ] Add timestamps for sections
- [ ] Test video plays correctly

---

## Distribution

### Where to Share
- **YouTube**: Upload with good SEO title and description
- **Team website**: Embed in documentation
- **Social media**: Share short clips (Twitter, Instagram)
- **Discord**: Share with team members

### Description Template

```markdown
**Title**: [Catchy, descriptive title]

**Description**:
Learn [concept] in [X] minutes! This video covers [topics] and is perfect for [beginner/intermediate/advanced] robot programmers.

**Chapters**:
0:00 - Introduction
0:30 - [Section 1]
2:00 - [Section 2]
4:00 - Summary

**Prerequisites**: [What viewer needs to know]
**Related Videos**: [Links to other videos]
**Documentation**: [Link to relevant docs]

**Questions?** Leave a comment or join our Discord!
```

---

## Call to Action Ideas

**End videos with**:
- "Try this yourself and let us know how it goes!"
- "What topics should we cover next? Comment below!"
- "Share this with your team if it helped!"
- "Join our community to ask questions!"
- "Check out the documentation for more details!"

---

**Remember**: Good educational videos are clear, concise, and filmed at a level that middle school students can understand. Keep it simple, show examples, and have fun!

**Need help making videos?** Check out our [Community Guidelines](/docs/COMMUNITY_GUIDELINES.md) for contributing content to MARSLib!