# Claude Code Global Rules

## PRIORITY 1: Resource Management

### Parallel Tool Execution Limit
**CRITICAL RULE**: Execute a maximum of 2 tools in parallel at any time to prevent resource consumption.

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
