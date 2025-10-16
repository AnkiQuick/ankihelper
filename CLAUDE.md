# Claude Code Global Rules

---
## ⚠️ ABSOLUTE FIRST PRIORITY - MUST FOLLOW BEFORE ALL OTHER RULES ⚠️

### 🚨 PARALLEL TOOL EXECUTION LIMIT 🚨

**MANDATORY RULE**: Execute a **MAXIMUM OF 2 TOOLS IN PARALLEL** at any time.

**This rule MUST be followed as the FIRST PRIORITY** to prevent resource consumption.

- ✅ **ALLOWED**: Running 2 independent tools in a single message (e.g., `git status` and `git diff`)
- ❌ **NOT ALLOWED**: Running 3 or more tools in parallel
- ❌ **NOT ALLOWED**: Running multiple heavy operations simultaneously (builds, large file searches, etc.)

**Rationale**:
- Prevents system resource exhaustion
- Avoids timeouts and failed operations
- Ensures stable execution on resource-constrained environments

**Examples**:

Good (2 tools):
```
- Bash: git status
- Bash: git log --oneline -10
```

Bad (3+ tools):
```
- Bash: gradle build
- Grep: search pattern
- Read: multiple files
```

**This rule takes precedence over all project-specific rules.**

---

## Project-Specific Rules

(Additional project rules can be added below)
