#!/usr/bin/env bash

# Memory Banking Infrastructure Setup
# Initializes or upgrades the AI memory banking system for accelerated engineering

set -e

SCRIPT_DIR="$(CDPATH="" cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

eval $(get_feature_paths)

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "  $1"
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    local all_good=true

    # Check git
    if command -v git >/dev/null 2>&1; then
        print_success "Git available"
    else
        print_error "Git not found - required for memory banking"
        all_good=false
    fi

    # Check we're in a repository
    if [[ -d "$REPO_ROOT/.git" ]] || git rev-parse --git-dir >/dev/null 2>&1; then
        print_success "Git repository detected"
    else
        print_warning "Not in a git repository - some features may not work"
    fi

    # Check basic shell tools
    for tool in find grep sed awk; do
        if command -v "$tool" >/dev/null 2>&1; then
            print_success "$tool available"
        else
            print_error "$tool not found - required for memory banking"
            all_good=false
        fi
    done

    if [[ "$all_good" == false ]]; then
        echo
        print_error "Prerequisites not met. Please install missing tools."
        exit 1
    fi

    echo
}

# Setup directory structure
setup_directories() {
    print_header "Setting Up Directory Structure"

    # Core memory directories
    local directories=(
        ".specify/memory"
        ".specify/templates"
        ".specify/scripts/bash"
        ".claude/commands"
        "specs"
    )

    for dir in "${directories[@]}"; do
        local full_path="$REPO_ROOT/$dir"
        if [[ ! -d "$full_path" ]]; then
            mkdir -p "$full_path"
            print_success "Created directory: $dir"
        else
            print_info "Directory exists: $dir"
        fi
    done

    echo
}

# Setup constitution
setup_constitution() {
    print_header "Setting Up Project Constitution"

    local constitution_file="$REPO_ROOT/.specify/memory/constitution.md"

    if [[ ! -f "$constitution_file" ]]; then
        # Create basic constitution template
        cat > "$constitution_file" << 'EOF'
# Project Constitution

## Core Principles

### I. [First Principle]
[Description of your first core principle]

### II. [Second Principle]
[Description of your second core principle]

### III. [Third Principle]
[Description of your third core principle]

## Technology Standards

[Define your technology stack requirements]

## Development Workflow

[Define your development process]

## Governance

[Define how decisions are made and principles are evolved]

**Version**: 1.0.0 | **Ratified**: $(date +%Y-%m-%d) | **Last Amended**: $(date +%Y-%m-%d)
EOF

        print_success "Created constitution template"
        print_info "Edit .specify/memory/constitution.md to define your principles"
    else
        # Check if it's still a template
        if grep -q "\[First Principle\]" "$constitution_file"; then
            print_warning "Constitution exists but appears to be template"
            print_info "Customize .specify/memory/constitution.md with your principles"
        else
            print_success "Constitution exists and is customized"
        fi
    fi

    echo
}

# Setup AI agent context
setup_agent_context() {
    print_header "Setting Up AI Agent Context"

    # Check for existing agent files
    local agent_files=("CLAUDE.md" "GEMINI.md" "AGENTS.md")
    local found_agents=()

    for agent_file in "${agent_files[@]}"; do
        if [[ -f "$REPO_ROOT/$agent_file" ]]; then
            found_agents+=("$agent_file")
        fi
    done

    if [[ ${#found_agents[@]} -eq 0 ]]; then
        # Create Claude context file as default
        if [[ -x "$SCRIPT_DIR/update-agent-context.sh" ]]; then
            print_info "Creating default Claude context file..."
            "$SCRIPT_DIR/update-agent-context.sh" claude
            print_success "Created CLAUDE.md context file"
        else
            print_warning "Agent context script not found or not executable"
        fi
    else
        print_success "Found existing agent files: ${found_agents[*]}"

        # Update existing context
        if [[ -x "$SCRIPT_DIR/update-agent-context.sh" ]]; then
            print_info "Updating agent context..."
            "$SCRIPT_DIR/update-agent-context.sh"
            print_success "Updated agent context files"
        fi
    fi

    echo
}

# Setup templates
setup_templates() {
    print_header "Setting Up Templates"

    local template_dir="$REPO_ROOT/.specify/templates"
    local templates_created=0

    # Agent file template
    if [[ ! -f "$template_dir/agent-file-template.md" ]]; then
        cat > "$template_dir/agent-file-template.md" << 'EOF'
# [PROJECT NAME] Development Guidelines

Auto-generated from all feature plans. Last updated: [DATE]

## Active Technologies

[EXTRACTED FROM ALL PLAN.MD FILES]

## Project Structure

```text
[ACTUAL STRUCTURE FROM PLANS]
```

## Commands

[ONLY COMMANDS FOR ACTIVE TECHNOLOGIES]

## Code Style

[LANGUAGE-SPECIFIC, ONLY FOR LANGUAGES IN USE]

## Recent Changes

[LAST 3 FEATURES AND WHAT THEY ADDED]

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
EOF
        ((templates_created++))
    fi

    # Spec template
    if [[ ! -f "$template_dir/spec-template.md" ]]; then
        cat > "$template_dir/spec-template.md" << 'EOF'
# Feature Specification: [Feature Name]

**Feature Branch**: `[branch-name]`
**Created**: [DATE]
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 - [Primary Use Case] (Priority: P1)

As a [user type], I need to [goal] so that I can [benefit].

**Acceptance Scenarios**:

1. **Given** [context], **When** [action], **Then** [outcome]
2. **Given** [context], **When** [action], **Then** [outcome]

---

### User Story 2 - [Secondary Use Case] (Priority: P2)

[Additional scenarios...]

## Technical Requirements *(mandatory)*

[Technical constraints, performance requirements, security considerations]

## Success Metrics *(mandatory)*

[How to measure if this feature is successful]
EOF
        ((templates_created++))
    fi

    # Plan template
    if [[ ! -f "$template_dir/plan-template.md" ]]; then
        cat > "$template_dir/plan-template.md" << 'EOF'
# Implementation Plan: [Feature Name]

## Technical Context *(mandatory)*

**Language/Version**: [Programming language and version]
**Primary Dependencies**: [Key frameworks, libraries]
**Storage**: [Database, file system, external services]
**Project Type**: [web app, API, library, etc.]

## Architecture Overview *(mandatory)*

[High-level design, major components, data flow]

## Implementation Phases *(mandatory)*

### Phase 1: [First Phase Name]
[What gets built in phase 1]

### Phase 2: [Second Phase Name]
[What gets built in phase 2]

## Risks & Mitigation *(mandatory)*

[Potential problems and how to address them]
EOF
        ((templates_created++))
    fi

    # Tasks template
    if [[ ! -f "$template_dir/tasks-template.md" ]]; then
        cat > "$template_dir/tasks-template.md" << 'EOF'
# Implementation Tasks: [Feature Name]

**Generated**: [DATE]
**Branch**: `[branch-name]`

## Task List

### Phase 1 Tasks

- [ ] **Task 1**: [Description]
  - **Estimate**: [effort estimate]
  - **Dependencies**: [other tasks or external dependencies]
  - **Definition of Done**: [completion criteria]

- [ ] **Task 2**: [Description]
  - **Estimate**: [effort estimate]
  - **Dependencies**: [other tasks or external dependencies]
  - **Definition of Done**: [completion criteria]

### Phase 2 Tasks

[Additional tasks...]

## Notes

[Any important implementation notes, gotchas, or decisions]
EOF
        ((templates_created++))
    fi

    if [[ $templates_created -gt 0 ]]; then
        print_success "Created $templates_created template(s)"
    else
        print_success "Templates already exist"
    fi

    echo
}

# Setup memory monitoring
setup_monitoring() {
    print_header "Setting Up Memory Monitoring"

    # Make memory status script executable
    local memory_status_script="$SCRIPT_DIR/memory-status.sh"
    if [[ -f "$memory_status_script" ]]; then
        chmod +x "$memory_status_script"
        print_success "Memory status script ready"
        print_info "Run: .specify/scripts/bash/memory-status.sh"
    else
        print_warning "Memory status script not found"
    fi

    # Make memory export script executable
    local memory_export_script="$SCRIPT_DIR/memory-export.sh"
    if [[ -f "$memory_export_script" ]]; then
        chmod +x "$memory_export_script"
        print_success "Memory export script ready"
        print_info "Run: .specify/scripts/bash/memory-export.sh status"
    else
        print_warning "Memory export script not found"
    fi

    echo
}

# Setup Claude Code integration
setup_claude_integration() {
    print_header "Setting Up Claude Code Integration"

    # Check for Claude settings
    local claude_settings="$REPO_ROOT/.claude/settings.local.json"
    if [[ -f "$claude_settings" ]]; then
        print_success "Claude Code settings found"

        # Check if memory banking commands are accessible
        local commands_dir="$REPO_ROOT/.claude/commands"
        if [[ -d "$commands_dir" ]]; then
            local command_count=$(find "$commands_dir" -name "*.md" | wc -l)
            print_success "$command_count Claude commands available"

            print_info "Available commands:"
            find "$commands_dir" -name "*.md" -exec basename {} \; | sed 's/speckit\./\//' | sed 's/.md$//' | sort | while read cmd; do
                print_info "  $cmd"
            done
        fi
    else
        print_warning "No Claude Code settings found"
        print_info "This is normal if not using Claude Code"
    fi

    echo
}

# Final health check
run_health_check() {
    print_header "Running Health Check"

    local memory_status_script="$SCRIPT_DIR/memory-status.sh"
    if [[ -x "$memory_status_script" ]]; then
        print_info "Running memory banking status check..."
        echo
        "$memory_status_script"
    else
        print_warning "Cannot run health check - memory status script not available"
    fi
}

# Print setup completion
print_completion() {
    print_header "Setup Complete!"

    echo -e "${GREEN}Memory Banking Infrastructure is ready!${NC}"
    echo
    echo "Next steps:"
    echo "1. Customize your constitution: .specify/memory/constitution.md"
    echo "2. Check system health: .specify/scripts/bash/memory-status.sh"
    echo "3. Create your first feature spec: /specify 'feature description'"
    echo "4. Plan implementation: /plan"
    echo "5. Generate tasks: /tasks"
    echo
    echo "Documentation:"
    echo "- Memory Banking Guide: .specify/memory/memory-banking-guide.md"
    echo "- Pattern Library: .specify/memory/patterns-library.md"
    echo
    echo -e "${BLUE}Happy engineering with AI acceleration!${NC}"
}

# Main execution
main() {
    echo -e "${BLUE}Memory Banking Infrastructure Setup${NC}"
    echo -e "${BLUE}==================================${NC}"
    echo

    check_prerequisites
    setup_directories
    setup_constitution
    setup_agent_context
    setup_templates
    setup_monitoring
    setup_claude_integration
    run_health_check
    print_completion
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi