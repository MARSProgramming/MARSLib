# MARSLib Documentation Templates

Use these templates when creating new documentation to ensure consistency, accessibility, and middle school reading level compliance.

## Template 1: Tutorial/Guide

### Frontmatter

```yaml
---
title: "Descriptive Title"
description: "One sentence description of what this guide covers."
sidebar:
  order: 1  # Adjust for ordering
  id: unique-id-here  # URL-friendly ID
  # Optional tags:
  tags: ["beginner", "intermediate", "advanced"]
---
```

### Structure

```markdown
# Title

**Hook**: Start with a simple question or problem that this guide solves.

## What You'll Learn

By the end of this guide, you'll understand:
- **Concept 1**: One sentence explanation
- **Concept 2**: One sentence explanation  
- **Concept 3**: One sentence explanation

## Prerequisites (if needed)

Before starting, you should know:
- **Skill 1**: Brief description
- **Skill 2**: Brief description

**Don't have these yet?** Check out [Prerequisite Guide](/link/to/prerequisite).

## Section 1: Simple Title

### Explanation
Start with the concept in plain language. What is this thing? Why do we need it?

**Example**: If explaining subsystems:
"A subsystem is like a department in a company. Just as a company has a sales department, an engineering department, and a finance department, your robot has different parts that do different jobs."

**In Other Words**: [Optional rephrase with analogy]
Think of it like [everyday comparison]...

### Code Example
```java
// Brief, focused example
// Explain each line
public class Example {
    // This line creates...
    // This line does...
}
```

### What This Does
- **Line 1**: What this specific line accomplishes
- **Line 2**: What this specific line accomplishes

### Common Mistakes
❌ **Wrong**: Common mistake
```java
// Show the mistake
```

✅ **Right**: Correct way
```java
// Show the fix
```

## Section 2: Next Concept

Follow the same pattern:
1. Plain language explanation
2. Simple analogy if helpful
3. Code example
4. What it does
5. Common mistakes

## Putting It All Together

Now let's combine everything:

```java
// Complete working example
// Explain how parts work together
```

### What This Does
Explain the complete example step by step.

## Testing

### How to Test
1. Step one
2. Step two
3. What to expect

### Expected Results
- ✅ **Result 1**: What should happen
- ✅ **Result 2**: What should happen

## Troubleshooting

### Problem 1
**Symptom**: What you see
**Solution**: How to fix it

### Problem 2  
**Symptom**: What you see
**Solution**: How to fix it

## Next Steps

Now that you've learned this, you can:
- [Next tutorial](/link/to/next)
- [Related guide](/link/to/related)
- [Advanced topic](/link/to/advanced)

---

**Ready for more?** Check out [Next Guide](/link/to/next)!
```

## Template 2: Quick Reference

### Frontmatter

```yaml
---
title: "Topic Quick Reference"  
description: "Get [task] done in [time] with this quick reference."
sidebar:
  order: 1
---
```

### Structure

```markdown
# Title Quick Reference

Quick intro sentence. Get [task] done fast with this guide.

## Essential Setup (X Steps)

### Step 1: Title
```java
// Minimal working code
```

**What this does**: Brief explanation

### Step 2: Title
```java
// Minimal working code
```

**What this does**: Brief explanation

## Common Tasks

### Task 1: Title
```java
// Code snippet
```

**Use when**: When you need to do X

### Task 2: Title
```java
// Code snippet  
```

**Use when**: When you need to do Y

## Quick Tips

✅ **Do**: Good practice
❌ **Don't**: Common mistake

## Troubleshooting

### Problem: Title
**Symptom**: What you see
**Fix**: Quick solution

## Related Guides
- [Full guide](/link/to/full/guide)
- [Related topic](/link/to/related)
```

## Template 3: Troubleshooting Guide

### Frontmatter

```yaml
---
title: "Problem Title - Troubleshooting Guide"
description: "Fix [problem] with this step-by-step guide."
sidebar:
  order: 1
---
```

### Structure

```markdown
# Problem Title - Troubleshooting Guide

**Symptom**: What the user sees or experiences

**Impact**: Why this matters (e.g., "Robot won't move during match")

## Quick Check (30 seconds)

### Check 1: Title
**What to look for**: What you should see
- ✅ **Good**: What correct looks like
- ❌ **Problem**: What wrong looks like

**If you see the problem**: Quick fix

### Check 2: Title
**What to look for**: What you should check

**If you see the problem**: Quick fix

## Detailed Diagnosis (5 minutes)

### Step 1: Title (1 minute)

**What we're checking**: Explanation

**Test**: ```java
// Debug code to add
```

**What to look for**:
- ✅ **Result**: What correct output means
- ❌ **Result**: What problem output means

### Step 2: Title (1 minute)
[Follow same pattern]

## Common Problems and Solutions

### Problem 1: Title
**Symptoms**: What you see
**Causes**: Why it happens

**Solution**:
```java
// Code fix
// Explanation of fix
```

### Problem 2: Title
[Follow same pattern]

## Quick Fixes to Try

### Fix 1: Title
1. Step one
2. Step two
3. Expected result

## Prevention Tips

### Before Competition:
- ✅ Preventative measure 1
- ✅ Preventative measure 2

### During Competition:
- ✅ Competition tip 1
- ✅ Competition tip 2

## Related Troubleshooting
- [Related problem guide](/troubleshooting/related)
- [General debugging](/operations/debugging)

---

**Still stuck?** [Get help](https://github.com/MARSProgramming/MARSLib/discussions)
```

## Template 4: API/Reference Documentation

### Frontmatter

```yaml
---
title: "Class Name Reference"
description: "Complete reference for ClassName - what it does and how to use it."
sidebar:
  order: 1
---
```

### Structure

```markdown
# ClassName Reference

**What it is**: Simple explanation of what this class does

**Analogy**: "Think of ClassName like [everyday comparison]..."

## Overview

**Purpose**: What this class accomplishes in your robot

**When to use it**: When you need to [specific task]

**Common uses**: List 2-3 most common uses

## Quick Example

```java
// Minimal working example
ClassName object = new ClassName();
object.doSomething();
```

### What This Does
- **Line 1**: Explanation
- **Line 2**: Explanation

## Constructor

### ClassName(parameters)
**What it does**: What the constructor creates

**Parameters**:
- `param1` (type): What this parameter does
- `param2` (type): What this parameter does

**Example**:
```java
ClassName obj = new ClassName(param1, param2);
```

## Methods

### methodName(parameters)
**What it does**: Simple explanation

**Parameters**:
- `param` (type): What this parameter does

**Returns**: What it returns (if anything)

**Example**:
```java
// Usage example
obj.methodName(value);
```

**Use cases**:
- When you need to [task 1]
- When you need to [task 2]

### methodName2(parameters)
[Follow same pattern for each public method]

## Common Patterns

### Pattern 1: Title
```java
// Complete code pattern
```

**When to use**: When you need to [task]

### Pattern 2: Title
```java
// Complete code pattern
```

**When to use**: When you need to [task]

## Examples

### Example 1: Title
**Goal**: What this example accomplishes

```java
// Complete working example
```

**What this does**: Step-by-step explanation

### Example 2: Title
[Follow same pattern]

## Tips and Best Practices

✅ **Do**: Good practice
```java
// Example
```

❌ **Don't**: Common mistake  
```java
// Example of mistake
```

## See Also

- [Related class](/api/RelatedClass)
- [Usage guide](/tutorials/how-to-use-this)
- [Examples](/examples/using-this-class)
```

## Writing Guidelines

### Reading Level Compliance

**Before publishing**, verify:
- [ ] Most sentences under 20 words
- [ ] Flesch-Kincaid grade level ≤ 8.0
- [ ] Technical terms explained when first used
- [ ] Active voice used (not passive)
- [ ] Concrete examples precede abstract concepts
- [ ] Analogies included for complex topics
- [ ] "In other words" sections for tricky concepts
- [ ] Paragraphs 2-3 sentences maximum
- [ ] Jargon minimized or explained
- [ ] Bulleted lists break up dense text

### Style Guidelines

**Sentences**:
- Keep under 20 words when possible
- One idea per sentence
- Active voice: "The motor moves the arm" (not "The arm is moved by the motor")

**Paragraphs**:
- 2-3 sentences maximum
- One main idea per paragraph
- Use bullet points for lists

**Technical Terms**:
- Explain on first use
- Use pronunciation guides for difficult terms: "Odometry (oh-DOM-it-ree)"
- Provide analogies: "PID controller is like a thermostat"

**Code Examples**:
- Keep focused and brief
- Comment important lines
- Show both wrong and right ways for common mistakes
- Explain what the code does, not just how

**Formatting**:
- Use bold for key terms
- Use code blocks for examples
- Use callout boxes for important notes
- Use tables for comparisons
- Use Mermaid diagrams for flows

### Content Structure

**Introduction**:
- Hook the reader with a problem or question
- Explain what they'll learn
- Set expectations for time/difficulty

**Body**:
- Start with concrete examples
- Move to abstract concepts
- Include "What this does" sections
- Add "In other words" summaries

**Conclusion**:
- Summarize key points
- Provide next steps
- Include links to related content

## Quality Checklist

Before submitting documentation, verify:

**Content**:
- [ ] Accurate and up-to-date
- [ ] Code examples work
- [ ] All links work
- [ ] Spelling and grammar correct
- [ ] Technical accuracy verified

**Accessibility**:
- [ ] Reading level ≤ 8.0 grade
- [ ] Technical terms explained
- [ ] Analogies included
- [ ] Multiple learning styles supported
- [ ] Visual elements (diagrams, code) included

**Structure**:
- [ ] Clear headings and sections
- [ ] Logical flow
- [ ] Complete examples
- [ ] Troubleshooting included (if applicable)
- [ ] Related content linked

**Testing**:
- [ ] Code tested in simulation
- [ ] Examples verified
- [ ] Links checked
- [ ] Reading level analyzed

## Tools for Writers

**Reading Level**:
- [Hemingway Editor](https://hemingwayapp.com/) - Highlights complex sentences
- [Readable.com](https://readable.com/) - Scores readability
- [Grammarly](https://www.grammarly.com/) - Checks clarity

**Code Quality**:
- VS Code - Syntax highlighting
- Spotless - Code formatting
- Gradle - Build verification

**Diagrams**:
- Mermaid - Flowcharts and sequence diagrams
- Draw.io - Visual diagrams
- Screenshots - For UI/documentation

---

**Remember**: The best documentation is so simple that a middle school student can understand it, but so accurate that an expert still respects it.

**Need help with documentation?** Check the [Contributing Guide](/contributing/getting-started) or ask in [Discussions](https://github.com/MARSProgramming/MARSLib/discussions).