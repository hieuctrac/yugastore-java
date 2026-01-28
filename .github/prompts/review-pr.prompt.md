---
description: "Review a PR for code quality, programmer mistakes, and coding standards compliance"
agent: pr-code-review
tools: ['codebase', 'search', 'read', 'usages', 'git', 'terminal']
---

# Review Pull Request

You are conducting a code review using the pr-code-review agent persona. Your task is to review a pull request and provide comprehensive feedback.

## PR to Review

PR Number: ${input:prNumber:Enter the PR number to review (e.g., 123)}

## Review Workflow

1. **Fetch PR Information**
   - Use `gh pr view ${prNumber} --repo hieuctrac/yugastore-java` to get PR details
   - Get the PR title, description, and changed files list

2. **Get Changed Files**
   - Use `gh pr diff ${prNumber} --repo hieuctrac/yugastore-java` to see the diff
   - Identify all modified files and the nature of changes

3. **Conduct Systematic Review**
   
   For each changed file, check:
   
   **Code Smells:**
   - Long methods (>50 lines)
   - Duplicated code
   - Large classes (>500 lines)
   - Feature envy
   - Primitive obsession
   - Over-commented code
   
   **Programmer Mistakes:**
   - Null pointer risks
   - Resource leaks
   - Incorrect exception handling
   - Magic numbers/strings
   - Off-by-one errors
   - Thread safety issues
   
   **YugaStore Coding Standards:**
   - Package structure: `com.yugabyte.app.yugastore.<microservice>.<layer>`
   - Naming conventions (PascalCase, camelCase, UPPER_SNAKE_CASE)
   - Spring Boot annotations (@RestController, @Service, constructor injection)
   - REST API design (proper URLs, HTTP methods, status codes)
   - Database integration (@Table, @Entity, @Query patterns)
   - Error handling (domain exceptions, proper logging)
   - Testing standards (naming, AAA pattern, mocking)
   - Documentation requirements
   - No System.out.println(), use proper logging
   - Optional usage for null handling
   
   **Security Issues:**
   - Input validation
   - No hardcoded secrets
   - SQL injection prevention
   - Authentication/authorization
   
   **Performance Issues:**
   - N+1 queries
   - Missing pagination
   - Inefficient queries
   - Resource usage

4. **Generate Review Summary**

   Structure your review as:

   ```markdown
   # Code Review Summary for PR #${prNumber}
   
   **PR Title:** [title from step 1]
   **Files Changed:** [count] files
   **Review Date:** [current date]
   
   ## Overview
   [Brief summary of what this PR does]
   
   ---
   
   ## ❌ Critical Issues (Must Fix Before Merge)
   
   [List each critical issue with:]
   - File and line number
   - Description of the problem
   - Why it's critical (bug risk, security, production impact)
   - Suggested fix with code example
   - Reference to coding standards if applicable
   
   ---
   
   ## ⚠️ Important Improvements (Should Fix)
   
   [List each important issue with:]
   - File and line number
   - Description of the code smell or issue
   - Impact on maintenance/performance
   - Suggested improvement
   
   ---
   
   ## 💡 Suggestions (Consider)
   
   [List optional improvements:]
   - Refactoring opportunities
   - Better naming
   - Documentation improvements
   
   ---
   
   ## ✅ Positive Feedback
   
   [Highlight good practices:]
   - Well-written code
   - Good test coverage
   - Proper error handling
   - Clear documentation
   
   ---
   
   ## Summary
   
   **Recommendation:** [APPROVE / REQUEST CHANGES / COMMENT]
   
   **Rationale:** [Explain your recommendation]
   
   **Next Steps:**
   - [What should be done before merge]
   ```

5. **Post Review to PR**
   
   Create the review comment using:
   ```bash
   gh pr review ${prNumber} --repo hieuctrac/yugastore-java --comment --body "[your review summary]"
   ```
   
   If there are critical issues, use `--request-changes`:
   ```bash
   gh pr review ${prNumber} --repo hieuctrac/yugastore-java --request-changes --body "[your review summary]"
   ```
   
   If everything looks good, use `--approve`:
   ```bash
   gh pr review ${prNumber} --repo hieuctrac/yugastore-java --approve --body "[your review summary]"
   ```

6. **Add Inline Comments** (if needed)
   
   For specific issues, add inline comments:
   ```bash
   gh pr review ${prNumber} --repo hieuctrac/yugastore-java --comment \
     --body "Issue description" \
     --file "path/to/file.java" \
     --line 42
   ```

## Review Guidelines

Remember to:
- ✅ Be constructive and respectful
- ✅ Explain why something is an issue, not just that it is
- ✅ Provide code examples for suggested fixes
- ✅ Reference the coding standards document when applicable
- ✅ Balance thoroughness with pragmatism
- ✅ Acknowledge good code and improvements
- ✅ Focus on substance over style preferences
- ✅ Consider the context and purpose of the PR

## Reference Documents

- [Coding Standards](../../docs/coding-standards.md)
- [Software Engineer Persona](../../docs/personas/software-engineer.md)
- [Architecture Documentation](../../docs/architecture.md)

---

**Note:** After the review is complete, provide a summary of:
1. Number of critical issues found
2. Number of important improvements suggested
3. Overall recommendation (approve/request changes/comment)
4. Whether the review was successfully posted to the PR
