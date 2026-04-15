# MARSLib Readability Guidelines

## Overview

All MARSLib documentation should be written at a **middle school reading level** (8th grade equivalent). This ensures accessibility for students of all ages and backgrounds, making robotics education inclusive and welcoming.

## Why Middle School Level?

- **Inclusivity**: FRC includes students from ages 14-18 with diverse reading abilities
- **Learning**: Clear explanations help everyone learn faster
- **Accessibility**: Supports students with learning differences, ADHD, or dyslexia
- **Global Reach**: Non-native English speakers benefit from simpler language
- **Efficiency**: Simple language communicates complex ideas more effectively

## Core Principles

### 1. Use Simple Words

**Choose everyday words over technical ones:**

❌ **Instead of**: "utilize"  
✅ **Use**: "use"

❌ **Instead of**: "ascertain"  
✅ **Use**: "find out" or "check"

❌ **Instead of**: "subsequently"  
✅ **Use**: "then" or "next"

❌ **Instead of**: "demonstrate"  
✅ **Use**: "show"

### 2. Keep Sentences Short

**Aim for 15-20 words maximum.** Break long sentences into shorter ones:

❌ **Too Long**: "The swerve drive subsystem utilizes four independent modules that each contain both a drive motor for propulsion and a steering motor for directional control, which enables the robot to move in any direction while rotating." (33 words)

✅ **Better**: "The swerve drive uses four independent modules. Each module has two motors: one for driving and one for steering. This lets the robot move in any direction while rotating." (25 words)

### 3. Use Active Voice

**Active voice is clearer and more direct:**

❌ **Passive**: "The robot is controlled by code that was written by the students."  
✅ **Active**: "Students write code to control the robot."

❌ **Passive**: "The sensor data is processed by the odometry system."  
✅ **Active**: "The odometry system processes sensor data."

### 4. Explain Technical Terms

**When you must use technical terms, explain them simply:**

**Example:**
```
## Odometry

Odometry (oh-DOM-it-ree) is how the robot knows where it is on the field. 
It's like counting steps to know how far you've walked. The robot uses 
wheel sensors to track movement and calculate its position.
```

### 5. Use Analogies

**Compare technical concepts to everyday things:**

- **PID Controller**: "Like a thermostat that maintains temperature by checking the current heat and adjusting the furnace"
- **Thread Safety**: "Like taking turns talking so everyone can understand the conversation"
- **Memory Allocation**: "Like reserving seats in a restaurant - you need enough tables but not too many"
- **CAN Bus**: "Like a conversation where devices take turns speaking on a shared wire"

### 6. Add "In Other Words" Sections

**Provide simple summaries after complex explanations:**

```markdown
## Zero-Allocation Programming

Zero-allocation means avoiding memory allocations in time-critical code loops.
This prevents the Java Garbage Collector from pausing your robot code.

**Technical Details**: The JVM garbage collector can cause 10-100ms pauses when
it cleans up unused memory objects. These pauses can make your robot stutter
or miss control loops.

**In Other Words**: Think of memory allocation like cleaning your room. If you
had to stop and clean every 20 seconds, you couldn't get anything done. 
Zero-allocation means keeping your room tidy as you go, so you never need
to stop and clean.
```

### 7. Use Concrete Examples First

**Start with specific examples, then explain the general concept:**

❌ **Abstract First**: "Encapsulation is a fundamental principle of object-oriented programming that hides internal implementation details..."

✅ **Concrete First**: "When you use a TV remote, you press buttons without knowing how the remote sends signals. The remote 'encapsulates' (hides) the complex electronics inside. In programming, we do the same thing - we hide complex code behind simple methods."

### 8. Format for Readability

**Use visual structure to make content easier to scan:**

- **Short paragraphs**: 2-3 sentences maximum
- **Bulleted lists**: For steps, options, or examples
- **Bold key terms**: Highlight important concepts
- **Code blocks**: Keep code examples short and focused
- **Clear headings**: Use descriptive titles

### 9. Avoid Jargon When Possible

**Replace technical jargon with everyday language:**

❌ **Instead of**: "Instantiate a new object"  
✅ **Use**: "Create a new object"

❌ **Instead of**: "Iterate through the array"  
✅ **Use**: "Go through each item in the list"

❌ **Instead of**: "Invoke the method"  
✅ **Use**: "Call the method"

### 10. Include Pronunciation Guides

**Help readers say technical terms:**

- "Odometry (oh-DOM-it-ree)"
- "Gyroscope (EYE-ro-scope)"
- "Accelerometer (ak-SEL-er-OM-uh-ter)"
- "IMU (Eye-Em-You)"
- "CAN (can, like a soda can)"

## Reading Level Checklist

Before publishing documentation, verify:

- [ ] Most sentences are under 20 words
- [ ] Technical terms are explained when first used
- [ ] Active voice is used (not passive)
- [ ] Concrete examples come before abstract concepts
- [ ] Analogies help explain complex ideas
- [ ] "In other words" sections simplify tricky concepts
- [ ] Paragraphs are short (2-3 sentences)
- [ ] Jargon is minimized or explained
- [ ] Bulleted lists break up dense text
- [ ] Code examples are focused and explained

## Tools for Measuring Readability

### Flesch-Kincaid Grade Level

- **Target**: 6.0 - 8.0 grade level
- **How to check**: Use online tools or writing software
- **What it measures**: Sentence length and word complexity

### Online Tools

- **Hemingway Editor**: Highlights complex sentences
- **Grammarly**: Suggests simpler word choices
- **Readable.com**: Scores text readability

## Examples

### Before (Too Complex)
```markdown
The IO Abstraction Layer implements a design pattern that decouples 
hardware-specific implementation details from high-level robot logic, 
facilitating seamless substitution of physical components with simulated 
alternatives for comprehensive testing without hardware dependencies.
```

### After (Middle School Level)
```markdown
## IO Abstraction Layer

The IO Abstraction Layer separates your robot code from the hardware. 
Think of it like a universal remote - the same remote works with different 
TV brands.

**Why This Matters:**
- Test code without a robot
- Change hardware easily
- Simulate robot behavior
- Write tests that run on any computer

**How It Works:**
Your robot code talks to an "interface" (a contract), not to specific hardware. 
The interface can connect to real motors or simulated ones. Your code doesn't 
need to know which one it's using!
```

## Accessibility Standards

These readability guidelines support our commitment to WCAG 2.1 AA accessibility:

- **Cognitive Accessibility**: Simple language helps users with processing differences
- **Learning Disabilities**: Clear structure supports dyslexic readers
- **Non-Native Speakers**: Simple English benefits international users
- **Attention Differences**: Short sections help readers with ADHD

## Getting Help

If you're unsure whether documentation meets these guidelines:

1. **Use Hemingway Editor** (free online) to check reading level
2. **Ask a middle school student** to read and explain it back
3. **Request a review** in GitHub Discussions
4. **Tag with `readability-review`** for community feedback

## Conclusion

Clear, simple documentation makes robotics accessible to everyone. By following these guidelines, you help create an inclusive learning environment where all students can succeed, regardless of their reading ability or background.

**Remember**: The best documentation is so simple that a middle school student can understand it, but so accurate that an expert still respects it.

---

*For more information, see our [Accessibility Commitment](/accessibility) and [Contributing Guide](/contributing/getting-started).*