# Community Contribution Guidelines

Help make MARSLib better for everyone! This guide shows how to contribute your improvements, examples, and simplifications.

## Why Contribute?

**For You**:
- ✅ Learn by teaching
- ✅ Build your skills
- ✅ Help your team
- ✅ Give back to community

**For Everyone**:
- ✅ Better documentation
- ✅ More examples
- ✅ Clearer explanations
- ✅ Stronger community

---

## Types of Contributions

### 1. Documentation Improvements

**What we need**:
- Simplified explanations
- Better analogies
- More examples
- Typos and grammar fixes
- Missing information

**Example contributions**:
- "I rewrote this explanation to be clearer"
- "I added an analogy that helped me understand"
- "I found a better way to explain this concept"

### 2. Code Examples

**What we need**:
- Working examples of common tasks
- Before/after comparisons
- Performance optimizations
- Edge case handling

**Example contributions**:
- "Here's how I implemented vision aiming"
- "This pattern worked better for me"
- "I found a simpler way to do this"

### 3. Troubleshooting Guides

**What we need**:
- Problems you've solved
- Solutions that worked
- Prevention tips
- Debugging strategies

**Example contributions**:
- "Here's how I fixed CAN bus errors"
- "This problem stumped me for hours"
- "I wish I knew this sooner"

### 4. Teaching Materials

**What we need**:
- Lesson plans
- Activities
- Quizzes
- Video scripts
- Analogies and explanations

**Example contributions**:
- "This activity helped my team learn subsystems"
- "I created a quiz for testing PID knowledge"
- "My students loved this analogy"

### 5. Translations

**What we need**:
- Multi-language documentation
- Cultural adaptations
- Local examples
- Regional resources

---

## How to Contribute

### Option 1: Quick Contributions (5-15 minutes)

**For small improvements**:

**1. Find something to improve**
- Read documentation
- Look for confusing parts
- Note what's missing

**2. Create your improvement**
- Rewrite confusing section
- Add missing example
- Fix typos/grammar
- Add better analogy

**3. Share it**
- Open GitHub Discussion
- Post your improvement
- Explain why it's better
- Get feedback

**4. We'll integrate it**
- Review your contribution
- Test if applicable
- Add to documentation
- Credit you for contribution

### Option 2: Pull Requests (30-60 minutes)

**For code or major documentation changes**:

**1. Fork MARSLib**
```bash
# Create your own copy
git clone https://github.com/YOUR_USERNAME/MARSLib.git
cd MARSLib
```

**2. Make your changes**
- Edit documentation files
- Add code examples
- Create new resources
- Test your changes

**3. Commit your changes**
```bash
git add .
git commit -m "Clear explanation of what you changed and why"
git push origin main
```

**4. Create Pull Request**
- Go to your fork on GitHub
- Click "Contribute" → "Open Pull Request"
- Describe your changes
- Link to any related issues
- Submit

**5. Respond to feedback**
- Address review comments
- Make requested changes
- Answer questions
- Celebrate when merged!

### Option 3: Discussion Posts (10-30 minutes)

**For sharing knowledge without editing files**:

**1. Start a discussion**
- Go to [MARSLib Discussions](https://github.com/MARSProgramming/MARSLib/discussions)
- Click "New Discussion"
- Choose appropriate category

**2. Share your contribution**
- Explain what you learned
- Provide examples
- Include before/after if relevant
- Make it reusable for others

**3. Engage with community**
- Answer questions
- Clarify when needed
- Update based on feedback
- Help others learn

---

## Contribution Guidelines

### Writing Style

**Follow these standards**:
- ✅ **Middle school reading level** (8th grade or lower)
- ✅ **Active voice** ("The motor moves" not "The motor is moved")
- ✅ **Concrete examples** before abstract concepts
- ✅ **Analogies** for complex ideas
- ✅ **Short sentences** (15-20 words max)
- ✅ **Plain language** (avoid jargon or explain it)

**Use this checklist**:
- [ ] Sentence length ≤ 20 words
- [ ] Flesch-Kincaid grade level ≤ 8.0
- [ ] Technical terms explained
- [ ] Analogies included for complex concepts
- [ ] Active voice used
- [ ] Concrete examples provided
- [ ] "In other words" sections for tricky concepts

### Code Examples

**Keep them**:
- ✅ **Short and focused**
- ✅ **Well-commented**
- ✅ **Tested and working**
- ✅ **Self-contained** (can run independently)
- ✅ **Properly formatted**

**Include**:
```java
// Brief explanation
public class Example {
    // What each part does
    // Why it works this way
}
```

### Explanations

**Structure**:
1. **What**: Simple definition
2. **Why**: Why it matters
3. **How**: How to use it
4. **Example**: Concrete example
5. **In other words**: Simple analogy

**Example**:
```markdown
## Odometry

**What**: Odometry (oh-DOM-it-ree) is how your robot knows where it is on the field.

**Why**: Without odometry, your robot wouldn't know its position for autonomous driving or field-relative control.

**How**: The robot counts wheel rotations to calculate distance traveled. It uses this to update its position estimate.

**Example**: Think of it like counting steps to know how far you've walked. If you know where you started and count every step, you can figure out where you are now.

**In other words**: It's like a pedometer for your robot!
```

---

## Contribution Ideas

### Beginner-Friendly Contributions

**Good first contributions**:
- Fix typos or grammar
- Add missing "what this does" explanations
- Create simple code examples
- Share analogies that helped you
- Document your learning journey

**Examples**:
- "I fixed 5 typos in the getting started guide"
- "I added examples for the shooter command"
- "Here's the analogy that finally made odometry click for me"

### Intermediate Contributions

**Medium complexity**:
- Rewrite confusing sections
- Add troubleshooting tips
- Create code challenges
- Write how-to guides
- Share performance optimizations

**Examples**:
- "I rewrote the PID section to be clearer"
- "Here are 5 common vision problems and how to fix them"
- "I created a challenge for teaching state machines"

### Advanced Contributions

**Complex work**:
- Create comprehensive guides
- Add new documentation sections
- Build interactive examples
- Create video tutorials
- Contribute code improvements

**Examples**:
- "I wrote a complete vision system guide"
- "I created interactive code exercises for beginners"
- "I made video scripts for all core concepts"

---

## Review Process

### What Happens When You Contribute

**1. Submission**
- You submit your contribution
- Automated checks run (if applicable)
- Maintainers are notified

**2. Review**
- Maintainers review your contribution
- Check against guidelines
- Test if applicable
- Provide feedback

**3. Revision (if needed)**
- Address review comments
- Make requested changes
- Clarify confusion
- Resubmit

**4. Integration**
- Contribution is merged
- Added to MARSLib
- Credit given to you
- Community benefits!

### Timeline

**Quick contributions** (Discussions):
- Usually visible immediately
- Community can provide feedback
- May be integrated into docs later

**Pull requests**:
- Review within 1-7 days
- May require revisions
- Merged when approved
- Appears in next release

---

## Recognition

### How Contributors Are Recognized

**In documentation**:
- Name in contributor list
- Credit for specific contributions
- Links to your work
- Featured in changelog

**In community**:
- Thanks in discussions
- Featured contributions
- Highlighted in updates
- Invitation to collaborate

**Long-term contributors**:
- Team member status
- Decision-making input
- Leadership opportunities
- Mentorship roles

### Contributor Levels

**Bronze Contributor** (1-3 contributions):
- Listed in contributors
- Thanked in release notes
- Community recognition

**Silver Contributor** (4-10 contributions):
- Featured contributor profile
- Invited to contributor discussions
- Priority support for your contributions

**Gold Contributor** (11+ contributions):
- Team member consideration
- Voting rights on project decisions
- Mentorship opportunities
- Conference presentation opportunities

---

## Contribution Ideas by Topic

### Documentation
- Simplify complex explanations
- Add missing examples
- Fix inconsistencies
- Improve flow and clarity
- Add cross-references

### Code
- Optimize slow code
- Add missing features
- Fix bugs
- Improve error messages
- Add safety checks

### Examples
- Real-world use cases
- Edge case examples
- Performance comparisons
- Before/after refactorings
- Integration examples

### Teaching
- Lesson plans
- Activities and exercises
- Quizzes and assessments
- Video scripts
- Analogies and metaphors

### Testing
- Test new features
- Find edge cases
- Report bugs
- Suggest improvements
- Verify fixes

---

## Tools and Resources

### For Writing

**Readability checkers**:
- [Hemingway Editor](https://hemingwayapp.com/) - Grade level analysis
- [Grammarly](https://www.grammarly.com/) - Grammar and style
- [Readable.com](https://readable.com/) - Multiple readability scores

**Code editors**:
- VS Code - Free, great for Markdown
- GitHub web editor - Quick edits in browser
- Typora - WYSIWYG Markdown editor

### For Testing

**Simulation**:
- MARSLib simulation environment
- Test without real hardware
- Verify code works

**Review tools**:
- Spell checkers
- Link checkers
- Markdown linters
- Code formatters

---

## Common Contribution Scenarios

### Scenario 1: "I found a confusing section"

**Contribution**:
1. Identify what's confusing
2. Rewrite for clarity
3. Test with someone else
4. Submit as Discussion or PR
5. Explain why it's clearer

### Scenario 2: "I have a better analogy"

**Contribution**:
1. Write your analogy
2. Explain the concept
3. Show how it helps
4. Share in Discussion
5. Get community feedback

### Scenario 3: "I solved a hard problem"

**Contribution**:
1. Document the problem
2. Explain your solution
3. Provide code example
4. Add troubleshooting tips
5. Submit as guide or Discussion

### Scenario 4: "I want to add a feature"

**Contribution**:
1. Check if already exists
2. Discuss in Issue first
3. Get design feedback
4. Implement feature
5. Submit PR with tests

---

## Quality Standards

### Before Submitting

**Check your contribution**:
- [ ] Reading level ≤ 8.0 grade
- [ ] All links work
- [ ] Code examples tested
- [ ] No typos or grammar errors
- [ ] Follows style guide
- [ ] Includes examples
- [ ] Explains "why" not just "what"

### After Submitting

**During review**:
- Be open to feedback
- Respond to comments
- Make revisions gracefully
- Ask for clarification
- Learn from process

---

## Getting Started

### Your First Contribution

**Choose something simple**:
- Fix a typo
- Add one example
- Clarify one explanation
- Share one analogy
- Answer one question

**Don't worry about perfection**:
- Contributions are reviewed
- Feedback helps you improve
- Community is supportive
- Every contribution helps

### Next Contributions

**Build on success**:
- Try something slightly harder
- Learn from previous contributions
- Develop your strengths
- Explore new areas
- Help others contribute

---

## Community Guidelines

### Be Respectful

**In all interactions**:
- Assume good intentions
- Give constructive feedback
- Accept feedback gracefully
- Help others improve
- Celebrate successes

### Be Patient

**With the process**:
- Review takes time
- Revisions are normal
- Learning takes practice
- Everyone was new once
- Quality over speed

### Be Inclusive

**In contributions**:
- Use accessible language
- Consider diverse audiences
- Provide multiple explanations
- Welcome different perspectives
- Value all contributions

---

## Need Help Contributing?

### Get Support

**Ask questions**:
- Start a Discussion
- Tag with "help wanted"
- Be specific about what you need
- Share your progress

**Get feedback**:
- Request review before submitting
- Share drafts
- Ask for guidance
- Learn from others

**Find mentors**:
- Experienced contributors
- Maintainers
- Community members
- Other contributors

---

## Celebrate Contributions

### When Your Contribution is Merged

**Share your success**:
- Tell your team
- Post on social media
- Update your resume
- Celebrate with contributors
- Plan next contribution

### When Others Contribute

**Show appreciation**:
- Thank contributors
- Review their work
- Provide encouragement
- Help them improve
- Celebrate their success

---

## Impact Tracking

### Your Contribution Matters

**Every contribution**:
- Helps another student learn
- Makes robotics more accessible
- Strengthens the community
- Improves the framework
- Inspires future contributors

**Track your impact**:
- Contributions made
- Issues resolved
- People helped
- Features added
- Documentation improved

---

## Ready to Contribute?

**Start here**:
1. Browse [MARSLib documentation](/)
2. Find something to improve
3. Make your contribution
4. Submit and get feedback
5. Celebrate your impact!

**Quick start**:
- Fix a typo you found
- Share an analogy that helped you
- Answer a question in Discussions
- Improve one explanation
- Add one example

**Remember**: The best way to learn is to teach. Contributing helps you learn while helping others. Everyone wins!

---

**Questions about contributing?** [Start a Discussion](https://github.com/MARSProgramming/MARSLib/discussions) - we're here to help you get started!

**Ready to make your first contribution?** Check out [Good First Issues](https://github.com/MARSProgramming/MARSLib/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) for ideas!