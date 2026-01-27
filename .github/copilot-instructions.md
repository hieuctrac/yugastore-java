# GitHub Copilot Instructions for YugaStore Java

## Repository Reference

When creating scripts, documentation, or any content that references a GitHub repository:

- **Default Repository**: Always use `hieuctrac/yugastore-java` as the target repository
- **Repository Owner**: `hieuctrac`
- **Repository Name**: `yugastore-java`

## Examples

### GitHub CLI Commands
```bash
gh issue create --repo hieuctrac/yugastore-java --title "Issue Title"
gh pr create --repo hieuctrac/yugastore-java --title "PR Title"
```

### Scripts
```bash
REPO="hieuctrac/yugastore-java"
```

### Documentation Links
```markdown
[View Issues](https://github.com/hieuctrac/yugastore-java/issues)
[View PRs](https://github.com/hieuctrac/yugastore-java/pulls)
```

### API Calls
```bash
curl https://api.github.com/repos/hieuctrac/yugastore-java/issues
```

Do not use `YugabyteDB-Samples/yugastore-java` unless explicitly requested by the user.

## GitHub Issues and Epics

When creating GitHub issues, epics, or user stories using `gh issue create`:

- **Do NOT use labels**: Never include the `--label` flag or specify any labels
- **Issue creation format**: Use only `--repo`, `--title`, and `--body` parameters

### Examples

**Correct:**
```bash
gh issue create --repo hieuctrac/yugastore-java --title "Epic: RBAC Implementation" --body "Description..."
gh issue create --repo hieuctrac/yugastore-java --title "US-1: JWT Authentication" --body "User story..."
```

**Incorrect:**
```bash
# DO NOT DO THIS - no labels allowed
gh issue create --repo hieuctrac/yugastore-java --title "Issue Title" --label "bug,priority:high" --body "..."
```
