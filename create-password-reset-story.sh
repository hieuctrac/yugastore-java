#!/bin/bash

# Script to create Password Reset User Story in GitHub
# Repository: hieuctrac/yugastore-java
# Links to EPIC Issue #1

set -e  # Exit on error

REPO="hieuctrac/yugastore-java"
EPIC_NUMBER="1"

echo "=========================================="
echo "Creating Password Reset User Story"
echo "Repository: $REPO"
echo "EPIC: #$EPIC_NUMBER"
echo "=========================================="
echo ""

# Check if gh CLI is authenticated
echo "Checking GitHub CLI authentication..."
if ! gh auth status &> /dev/null; then
    echo "❌ Error: GitHub CLI is not authenticated"
    echo "Please run: gh auth login"
    exit 1
fi
echo "✓ Authenticated"
echo ""

# Create US-10: Password Reset Workflow
echo "Creating US-10: Password Reset Workflow..."
US10_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-10: Password Reset Workflow" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH (Customer Experience)
**Story Points:** 3

## User Story

**As a** customer  
**I want to** reset my password when I forget it  
**So that** I can regain access to my account without contacting support

## Description

Implement a secure self-service password reset workflow that allows users to reset forgotten passwords via email verification. This reduces support burden and improves customer experience by enabling immediate account recovery.

## Acceptance Criteria

- [ ] \"Forgot Password\" link displayed on login page
- [ ] Password reset request endpoint \`POST /auth/forgot-password\` accepts email address
- [ ] System generates secure password reset token (UUID, expires in 1 hour)
- [ ] Password reset token stored in database with user ID and expiration timestamp
- [ ] Password reset email sent to user with reset link containing token
- [ ] Reset link format: \`https://yugastore.com/reset-password?token={token}\`
- [ ] Password reset page \`GET /reset-password\` displays form when valid token provided
- [ ] Invalid/expired tokens show clear error message
- [ ] Password reset completion endpoint \`POST /auth/reset-password\` validates token and updates password
- [ ] New password validated (minimum 8 characters, required by user)
- [ ] Password securely hashed (bcrypt) before storage
- [ ] Token invalidated after successful password reset
- [ ] User can only have one active reset token at a time (new request invalidates previous)
- [ ] Confirmation email sent after successful password reset
- [ ] Rate limiting: max 3 reset requests per email per hour (prevent abuse)

## Technical Notes

**Database Schema:**
\`\`\`sql
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INT NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES username(id)
);

CREATE INDEX idx_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_reset_user_expiry ON password_reset_tokens(user_id, expires_at);
\`\`\`

**Email Template:**
\`\`\`
Subject: Reset Your YugaStore Password

Hi {username},

We received a request to reset your password. Click the link below to create a new password:

{reset_link}

This link will expire in 1 hour.

If you didn't request this, please ignore this email. Your password will remain unchanged.

Thanks,
The YugaStore Team
\`\`\`

**Security Considerations:**
- Use cryptographically secure random token generation
- Tokens expire after 1 hour
- Rate limit password reset requests (3 per hour per email)
- No user enumeration: same response whether email exists or not
- Log all password reset attempts for security monitoring
- Invalidate all existing sessions after password reset (optional but recommended)

**API Endpoints:**
1. \`POST /auth/forgot-password\`
   - Body: \`{\"email\": \"user@example.com\"}\`
   - Response: 200 OK (always, even if email not found to prevent enumeration)
   
2. \`GET /reset-password?token={token}\`
   - Validates token and displays password reset form
   - Returns 400 if token invalid/expired
   
3. \`POST /auth/reset-password\`
   - Body: \`{\"token\": \"...\", \"newPassword\": \"...\"}\`
   - Response: 200 OK with success message

## UI/UX Requirements

- [ ] \"Forgot Password\" link prominently displayed on login page
- [ ] Password reset request page with clear instructions
- [ ] Success message after reset email sent (generic, no user enumeration)
- [ ] Password reset form with password strength indicator
- [ ] Clear error messages for expired/invalid tokens
- [ ] Success confirmation after password reset with link to login page

## Testing

**Unit Tests:**
- Test token generation and validation
- Test token expiration logic
- Test password hashing
- Test rate limiting

**Integration Tests:**
- Test complete password reset flow
- Test expired token handling
- Test invalid token handling
- Test rate limiting enforcement
- Test email delivery
- Test user enumeration prevention

**Security Tests:**
- Verify tokens cannot be guessed
- Verify expired tokens are rejected
- Verify rate limiting works
- Verify no user enumeration possible
- Verify old sessions invalidated after reset

## Dependencies

- US-1 (JWT Authentication) must be complete
- Email service configured and working
- Frontend forms for password reset flow

## Related User Stories

- US-1: JWT-Based Authentication Infrastructure (foundation)
- US-2: Role Management and Assignment (user lookup)

## Definition of Done

- [ ] Password reset endpoints implemented and tested
- [ ] Database table for reset tokens created
- [ ] Email templates created and tested
- [ ] Rate limiting implemented
- [ ] Security measures validated (no enumeration, token security)
- [ ] Frontend forms integrated
- [ ] Unit and integration tests passing
- [ ] Security review completed
- [ ] Documentation updated (API docs, user guide)
- [ ] Code reviewed and merged

## Notes

**Business Value:**
- Reduces support ticket volume for password resets (estimated 20-30% reduction)
- Improves customer experience with immediate self-service recovery
- Meets industry standard security practices
- Reduces friction in customer reactivation

**Estimated Support Impact:**
Based on industry benchmarks, password reset functionality typically reduces support contacts by 25% for authentication-related issues.")

US10_NUMBER=$(echo "$US10_URL" | grep -o '[0-9]*$')

if [ -z "$US10_NUMBER" ]; then
    echo "❌ Error: Failed to extract issue number from URL: $US10_URL"
    exit 1
fi

echo "✓ US-10 created: #$US10_NUMBER"
echo "  URL: $US10_URL"
echo ""

echo "=========================================="
echo "✅ Password Reset User Story Created"
echo "=========================================="
echo ""
echo "Summary:"
echo "  - US-10: Password Reset Workflow (#$US10_NUMBER)"
echo "  - Linked to EPIC #$EPIC_NUMBER"
echo "  - Story Points: 3"
echo "  - Priority: HIGH"
echo ""
echo "Next Steps:"
echo "  1. Review the user story at: $US10_URL"
echo "  2. Add to sprint backlog for Phase 1 implementation"
echo "  3. Update RBAC EPIC description to reference US-10"
echo ""
