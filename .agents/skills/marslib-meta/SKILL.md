---
name: marslib-meta
description: Meta-framework guidance including team culture, educational tutoring, and skill authoring standards. Use when writing docs, teaching concepts, or creating new skills.
---

# MARSLib Meta Framework

## 1. Team Culture & Legacy

**MARS 2614** — 2017 FIRST Championship Chairman's Award winners, FIRST Hall of Fame.

### Core Values
When writing documentation or discussing framework philosophy:
- **Discovery** — Highlight creative persistence solving complex problems
- **Innovation** — Celebrate technical achievements
- **Impact** — Show how MARSLib empowers teams
- **Teamwork** — Foster collaboration and inclusion
- **Inclusion** — Ensure accessibility for all skill levels
- **Fun** — Celebrate technical successes

### Phil Tucker Ethos
- **Motto:** "No robot left behind."
- **Use when:** Discussing accessibility, zero-mocking simulation, readable logging

### Chairman's Legacy ("The MARS Plan")
- **Motto:** "Engage, Inspire, Sustain, Progression of Programs, Creating Leaders/Innovators."
- **Use when:** Explaining tutorial progression or framework mission

### Core Vehicle Philosophy
- **Motto:** "The robots are the vehicle; the students are the cargo."
- **Use when:** Rejecting black-box solutions in favor of teachable architectures

### Tone
- **Do not be arrogant** — We share because we're Hall of Fame, leading by example
- **Weave naturally** — Place quotes where relevant (intros, philosophy sections), not randomly

## 2. Educational Tutoring

When this skill is active (user asks to learn/understand):
- **Override efficiency for teaching** — Provide detailed explanations
- **Always explain the "why"** — Not just code, but architectural reasoning
- **Use Socratic method** — Guide with questions, not just answers
- **Progressive disclosure** — Start simple, introduce advanced gradually
- **Relatable analogies** — Link abstract concepts to real-world examples
- **End with celebration** — Summarize key takeaways, celebrate Innovation achieved

## 3. Skill Authoring

### When to Create a Skill
Create when:
- New package added to `com.marslib.*`
- Major reusable utility added to `com.marslib.util`
- New hardware integration layer introduced
- Existing skill exceeds ~120 lines (split it)

Do NOT create for:
- Individual commands or one-off helpers
- Bug fixes or test files
- Constants changes

### File Location & Naming
```
.agents/skills/marslib-{name}/SKILL.md
```
- Prefix: `marslib-`
- Suffix: lowercase, hyphen-separated domain name
- Match Java package name where practical

### SKILL.md Template
```markdown
---
name: marslib-{name}
description: {One-sentence ending with "Use when..." trigger}
---

# MARSLib {Title} Skill

{One-line persona and scope statement.}

## 1. Architecture
{Class relationships, IO pattern, DI wiring}

## 2. Key Rules
{Non-obvious constraints that cause bugs}

## 3. Adding New {States/Components}
{Step-by-step extension instructions}

## 4. Telemetry
{All Logger.recordOutput() keys}
```

### Content Quality Rules
1. **Be prescriptive** — "You MUST do X" not "X is possible"
2. **Include code snippets** for non-obvious API usage
3. **Reference file paths** for navigation
4. **Document failure modes** — "If violated, Y happens"
5. **Keep under 120 lines** — Split if longer
6. **Integrate Core Values** where appropriate
7. **Use `_FIRST_®` branding** correctly (italics, all caps, trademark)

### After Creating a Skill
1. Verify skill is discoverable in `.agents/skills/marslib-{name}/SKILL.md`
2. Create `plugin.json` (use existing as template)
3. Add to `marketplace.json`
4. Update `.cursorrules` and `.github/copilot-instructions.md`
5. Cross-reference from related skills
6. Add tests if system-specific patterns exist

### Updating Existing Skills
When code changes:
1. **Always update the skill** alongside the code
2. Add new telemetry keys to Telemetry section
3. Update transition graphs if state machines change
4. Bump "Adding New..." section if process changed

Never delete a skill unless the entire system is removed.
