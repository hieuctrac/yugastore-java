# AI Memory Banking Guide for YugaStore

This guide explains how to leverage the three-tier memory banking system for accelerated engineering.

## Memory Architecture Overview

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   SHORT TERM        │    │   MEDIUM TERM       │    │   LONG TERM         │
│   (Current Context) │    │   (Project Memory)  │    │   (Pattern Library) │
├─────────────────────┤    ├─────────────────────┤    ├─────────────────────┤
│ • CLAUDE.md         │◄──►│ • specs/*/          │◄──►│ • constitution.md   │
│ • Active branch     │    │ • Feature plans     │    │ • templates/        │
│ • Current tasks     │    │ • Research findings │    │ • Command patterns  │
│ • Recent changes    │    │ • Data models       │    │ • Best practices    │
│ • Session state     │    │ • Contracts         │    │ • Cross-project     │
└─────────────────────┘    └─────────────────────┘    └─────────────────────┘
     Updates every           Updates per feature        Updates per project
     interaction             (weeks/months)             (months/years)
```

## How to Use Each Memory Level

### SHORT TERM MEMORY (Seconds to Minutes)

**What it does**: Maintains current working context across AI interactions

**Files managed**:
- `CLAUDE.md` - Auto-updated context file
- Current branch state
- Active technologies in use
- Recent changes (last 3 features)

**How to use**:
```bash
# Context is auto-updated when you work on features
# View current context
cat CLAUDE.md

# Manual context update (if needed)
.specify/scripts/bash/update-agent-context.sh claude

# Check what AI agents are configured
ls -la *.md | grep -E "(CLAUDE|GEMINI|AGENTS)"
```

**Best practices**:
- Trust the auto-update system - it tracks your work
- Check CLAUDE.md when starting new sessions
- Use manual updates only when switching contexts rapidly

### MEDIUM TERM MEMORY (Hours to Days)

**What it does**: Accumulates project knowledge and architectural decisions

**Files managed**:
- `specs/[feature]/spec.md` - Feature requirements
- `specs/[feature]/plan.md` - Implementation plans
- `specs/[feature]/research.md` - Technical research
- `specs/[feature]/data-model.md` - Data structures
- `specs/[feature]/contracts/` - API specifications
- `specs/[feature]/tasks.md` - Implementation tasks

**How to use**:
```bash
# Create new feature specification
/specify "Add user authentication system"

# Plan implementation approach
/plan "Implement JWT-based authentication"

# Generate actionable tasks
/tasks

# Create implementation checklist
/checklist
```

**Best practices**:
- Always start with `/specify` to capture requirements
- Use `/plan` before coding to think through architecture
- Reference previous specs when building related features
- Keep research findings for future reference

### LONG TERM MEMORY (Days to Months)

**What it does**: Preserves organizational knowledge and reusable patterns

**Files managed**:
- `.specify/memory/constitution.md` - Project principles
- `.specify/templates/` - Reusable templates
- `.claude/commands/` - Workflow patterns
- Cross-project learnings

**How to use**:
```bash
# Update project constitution
/constitution "Add new principle about API design"

# View available templates
ls .specify/templates/

# Check available workflow commands
ls .claude/commands/
```

**Best practices**:
- Document architectural decisions in constitution
- Create templates for repeated patterns
- Evolve constitution based on project learnings
- Share patterns across team/projects

## Memory Flow Example

```mermaid
graph TD
    A[New Feature Request] --> B[/specify - Create spec.md]
    B --> C[/plan - Generate plan.md]
    C --> D[/tasks - Create tasks.md]
    D --> E[/implement - Execute tasks]
    E --> F[Auto-update CLAUDE.md]
    F --> G[Feature Complete]
    G --> H[Update Constitution]
    H --> I[Create New Templates]

    subgraph "Short Term"
    F
    end

    subgraph "Medium Term"
    B
    C
    D
    end

    subgraph "Long Term"
    H
    I
    end
```

## Current Memory State

### Short Term Context
- **Active Branch**: memory-bank
- **Previous Branch**: step-0a
- **Current Technologies**: Java 17, Spring Boot 2.6.3, YugabyteDB
- **Last Feature**: 002-admin-portal (Admin Portal for Product Management)

### Medium Term Knowledge
- **Active Specs**:
  - 001-rbac-implementation (RBAC authentication)
  - 002-admin-portal (Product management interface)
- **Architectural Decisions**:
  - Microservices with Spring Cloud
  - Dual database strategy (YSQL + YCQL)
  - JWT-based authentication
  - Service mesh with Eureka

### Long Term Patterns
- **Constitution**: Template ready for customization
- **Workflow Commands**: 9 SpecKit commands available
- **Templates**: Agent files, checklists, plans, specs, tasks
- **Cross-Agent Support**: 15+ AI development environments

## Memory Banking Best Practices

### 1. Context Hygiene
- Let the system auto-manage short term memory
- Regularly review and clean up old specs
- Keep constitution up to date with learnings

### 2. Knowledge Layering
- Start broad (spec) → narrow down (plan) → execute (tasks)
- Build new features on previous architectural decisions
- Reference past research to avoid repeating work

### 3. Pattern Recognition
- Notice repeated implementation patterns
- Extract common patterns into templates
- Share successful approaches via constitution

### 4. Cross-Session Continuity
- Always check CLAUDE.md when resuming work
- Review recent features to understand context
- Use `/clarify` to fill knowledge gaps

### 5. Team Collaboration
- Constitution serves as team contract
- Specs document decisions for future team members
- Templates ensure consistent implementation patterns

## Advanced Memory Operations

### Context Debugging
```bash
# View current memory state
echo "=== SHORT TERM ==="
head -20 CLAUDE.md

echo "=== MEDIUM TERM ==="
find specs -name "*.md" -newer $(date -d '1 month ago' +%Y%m%d) -ls

echo "=== LONG TERM ==="
wc -l .specify/memory/constitution.md .specify/templates/*.md
```

### Memory Validation
```bash
# Check memory consistency
.specify/scripts/bash/check-prerequisites.sh

# Validate current feature context
cat specs/*/spec.md | grep -i "status:"
```

### Memory Recovery
```bash
# Rebuild context if corrupted
.specify/scripts/bash/update-agent-context.sh

# Restore from git history
git log --oneline .specify/ specs/ CLAUDE.md
```

## Memory Banking Metrics

Track these indicators of memory system health:

- **Context Accuracy**: How often CLAUDE.md reflects current work
- **Knowledge Reuse**: References to previous specs/research
- **Pattern Evolution**: Updates to constitution and templates
- **Cross-Feature Learning**: How new features build on previous work

## Troubleshooting Memory Issues

### Problem: AI loses context mid-session
**Solution**: Check if CLAUDE.md is stale, run manual context update

### Problem: Repeating previous architectural mistakes
**Solution**: Review constitution, update with new principles

### Problem: Inconsistent implementation patterns
**Solution**: Extract successful patterns into templates

### Problem: Lost project knowledge when team members leave
**Solution**: Ensure all decisions documented in specs and constitution

---

**Remember**: Memory Banking is about preserving and building on knowledge, not just storing information. Each interaction should either use existing knowledge or contribute new knowledge back to the system.