#!/usr/bin/env bash

# Memory Banking System Status Monitor
# Provides health checks and metrics for the three-tier memory system

set -e

# Get script directory and load common functions
SCRIPT_DIR="$(CDPATH="" cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# Get paths
eval $(get_feature_paths)

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# SHORT TERM MEMORY STATUS
check_short_term_memory() {
    print_header "SHORT TERM MEMORY STATUS"

    # Check agent context files
    local agent_files=("CLAUDE.md" "GEMINI.md" "AGENTS.md" ".cursor/rules/specify-rules.mdc" ".windsurf/rules/specify-rules.md")
    local found_agents=0

    for agent_file in "${agent_files[@]}"; do
        if [[ -f "$REPO_ROOT/$agent_file" ]]; then
            local last_updated=$(stat -f "%Sm" -t "%Y-%m-%d %H:%M" "$REPO_ROOT/$agent_file" 2>/dev/null || echo "unknown")
            print_success "Agent context: $agent_file (updated: $last_updated)"
            ((found_agents++))
        fi
    done

    if [[ $found_agents -eq 0 ]]; then
        print_error "No agent context files found"
    else
        print_info "Active AI agents: $found_agents"
    fi

    # Check current context freshness
    if [[ -f "$REPO_ROOT/CLAUDE.md" ]]; then
        local claude_age=$(find "$REPO_ROOT" -name "CLAUDE.md" -mtime +1 | wc -l)
        if [[ $claude_age -gt 0 ]]; then
            print_warning "CLAUDE.md is older than 1 day - may need refresh"
        else
            print_success "CLAUDE.md is up to date"
        fi

        # Check content quality
        local tech_count=$(grep -c "^- " "$REPO_ROOT/CLAUDE.md" || echo "0")
        local recent_changes=$(grep -A 5 "## Recent Changes" "$REPO_ROOT/CLAUDE.md" | grep -c "^- " || echo "0")

        print_info "Active technologies tracked: $tech_count"
        print_info "Recent changes recorded: $recent_changes"
    fi

    # Check current branch context
    if [[ -n "$CURRENT_BRANCH" ]]; then
        print_success "Current context: $CURRENT_BRANCH"
    else
        print_warning "No current branch/feature context"
    fi

    echo
}

# MEDIUM TERM MEMORY STATUS
check_medium_term_memory() {
    print_header "MEDIUM TERM MEMORY STATUS"

    # Check specs directory structure
    if [[ -d "$SPECS_DIR" ]]; then
        local feature_count=$(find "$SPECS_DIR" -mindepth 1 -maxdepth 1 -type d | wc -l)
        print_success "Feature specifications directory exists"
        print_info "Total features: $feature_count"

        # List recent features
        print_info "Recent features:"
        find "$SPECS_DIR" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; | sort | tail -5 | while read feature; do
            if [[ -f "$SPECS_DIR/$feature/spec.md" ]]; then
                local status=$(grep "^**Status**:" "$SPECS_DIR/$feature/spec.md" | sed 's/^**Status**: //' || echo "Unknown")
                print_info "  - $feature ($status)"
            else
                print_info "  - $feature (no spec.md)"
            fi
        done

        # Check for complete feature documentation
        local complete_features=0
        local incomplete_features=0

        for feature_dir in "$SPECS_DIR"/*; do
            if [[ -d "$feature_dir" ]]; then
                local feature_name=$(basename "$feature_dir")
                local has_spec=$([[ -f "$feature_dir/spec.md" ]] && echo 1 || echo 0)
                local has_plan=$([[ -f "$feature_dir/plan.md" ]] && echo 1 || echo 0)

                if [[ $has_spec -eq 1 && $has_plan -eq 1 ]]; then
                    ((complete_features++))
                else
                    ((incomplete_features++))
                fi
            fi
        done

        print_info "Complete features (spec + plan): $complete_features"
        if [[ $incomplete_features -gt 0 ]]; then
            print_warning "Incomplete features: $incomplete_features"
        fi

    else
        print_error "No specs directory found at $SPECS_DIR"
    fi

    # Check current feature status
    if [[ -n "$CURRENT_BRANCH" && -f "$IMPL_PLAN" ]]; then
        print_success "Current feature has implementation plan"

        # Check plan completeness
        local needs_clarification=$(grep -c "NEEDS CLARIFICATION" "$IMPL_PLAN" 2>/dev/null || echo "0")
        if [[ $needs_clarification -gt 0 ]]; then
            print_warning "Current plan has $needs_clarification items needing clarification"
        else
            print_success "Current plan is fully specified"
        fi
    elif [[ -n "$CURRENT_BRANCH" ]]; then
        print_warning "Current feature ($CURRENT_BRANCH) missing implementation plan"
    fi

    echo
}

# LONG TERM MEMORY STATUS
check_long_term_memory() {
    print_header "LONG TERM MEMORY STATUS"

    # Check constitution
    local constitution_file="$REPO_ROOT/.specify/memory/constitution.md"
    if [[ -f "$constitution_file" ]]; then
        local const_version=$(grep "^**Version**:" "$constitution_file" | sed 's/^**Version**: //' | cut -d'|' -f1 | xargs || echo "Unknown")
        local const_date=$(grep "^**Version**:" "$constitution_file" | grep -o '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' | tail -1 || echo "Unknown")

        print_success "Project constitution exists (v$const_version, $const_date)"

        # Check if it's still template
        if grep -q "\[PROJECT_NAME\]" "$constitution_file"; then
            print_warning "Constitution appears to be template (needs customization)"
        else
            local principle_count=$(grep -c "^### " "$constitution_file" || echo "0")
            print_info "Defined principles: $principle_count"
        fi
    else
        print_error "No project constitution found"
    fi

    # Check templates
    local template_dir="$REPO_ROOT/.specify/templates"
    if [[ -d "$template_dir" ]]; then
        local template_count=$(find "$template_dir" -name "*.md" | wc -l)
        print_success "Template library exists"
        print_info "Available templates: $template_count"

        # List templates
        find "$template_dir" -name "*.md" -exec basename {} \; | sed 's/.md$//' | sort | while read template; do
            print_info "  - $template"
        done
    else
        print_error "No template directory found"
    fi

    # Check command patterns
    local commands_dir="$REPO_ROOT/.claude/commands"
    if [[ -d "$commands_dir" ]]; then
        local command_count=$(find "$commands_dir" -name "*.md" | wc -l)
        print_success "Command patterns library exists"
        print_info "Available commands: $command_count"

        # List commands
        find "$commands_dir" -name "*.md" -exec basename {} \; | sed 's/speckit\.//' | sed 's/.md$//' | sort | while read cmd; do
            print_info "  - /$cmd"
        done
    else
        print_warning "No command patterns directory found"
    fi

    # Check for memory guide
    local memory_guide="$REPO_ROOT/.specify/memory/memory-banking-guide.md"
    if [[ -f "$memory_guide" ]]; then
        print_success "Memory banking guide exists"
    else
        print_warning "No memory banking guide found"
    fi

    echo
}

# MEMORY HEALTH METRICS
calculate_memory_metrics() {
    print_header "MEMORY HEALTH METRICS"

    local score=0
    local max_score=10

    # Short term metrics (3 points)
    if [[ -f "$REPO_ROOT/CLAUDE.md" ]]; then
        score=$((score + 1))
        local claude_age=$(find "$REPO_ROOT" -name "CLAUDE.md" -mtime -1 | wc -l)
        if [[ $claude_age -gt 0 ]]; then
            score=$((score + 1))
        fi
    fi
    if [[ -n "$CURRENT_BRANCH" ]]; then
        score=$((score + 1))
    fi

    # Medium term metrics (4 points)
    if [[ -d "$SPECS_DIR" ]]; then
        score=$((score + 1))
        local feature_count=$(find "$SPECS_DIR" -mindepth 1 -maxdepth 1 -type d | wc -l)
        if [[ $feature_count -gt 0 ]]; then
            score=$((score + 1))
        fi
        if [[ -n "$CURRENT_BRANCH" && -f "$IMPL_PLAN" ]]; then
            score=$((score + 1))
        fi
        local needs_clarification=0
        if [[ -f "$IMPL_PLAN" ]]; then
            needs_clarification=$(grep -c "NEEDS CLARIFICATION" "$IMPL_PLAN" 2>/dev/null || echo "0")
        fi
        if [[ $needs_clarification -eq 0 ]]; then
            score=$((score + 1))
        fi
    fi

    # Long term metrics (3 points)
    local constitution_file="$REPO_ROOT/.specify/memory/constitution.md"
    if [[ -f "$constitution_file" ]] && ! grep -q "\[PROJECT_NAME\]" "$constitution_file"; then
        score=$((score + 1))
    fi
    if [[ -d "$REPO_ROOT/.specify/templates" ]]; then
        score=$((score + 1))
    fi
    if [[ -d "$REPO_ROOT/.claude/commands" ]]; then
        score=$((score + 1))
    fi

    # Calculate percentage
    local percentage=$((score * 100 / max_score))

    print_info "Memory Health Score: $score/$max_score ($percentage%)"

    if [[ $percentage -ge 90 ]]; then
        print_success "Excellent memory banking system health"
    elif [[ $percentage -ge 70 ]]; then
        print_success "Good memory banking system health"
    elif [[ $percentage -ge 50 ]]; then
        print_warning "Moderate memory banking system health"
    else
        print_error "Poor memory banking system health - needs attention"
    fi

    echo
}

# RECOMMENDATIONS
provide_recommendations() {
    print_header "RECOMMENDATIONS"

    # Check for common issues and provide fixes
    local recommendations=()

    if [[ ! -f "$REPO_ROOT/CLAUDE.md" ]]; then
        recommendations+=("Run: .specify/scripts/bash/update-agent-context.sh claude")
    fi

    local constitution_file="$REPO_ROOT/.specify/memory/constitution.md"
    if [[ -f "$constitution_file" ]] && grep -q "\[PROJECT_NAME\]" "$constitution_file"; then
        recommendations+=("Customize project constitution with your principles")
    fi

    if [[ -z "$CURRENT_BRANCH" ]]; then
        recommendations+=("Work on a feature branch for better context tracking")
    fi

    if [[ -n "$CURRENT_BRANCH" && ! -f "$IMPL_PLAN" ]]; then
        recommendations+=("Create implementation plan: /plan")
    fi

    local needs_clarification=0
    if [[ -f "$IMPL_PLAN" ]]; then
        needs_clarification=$(grep -c "NEEDS CLARIFICATION" "$IMPL_PLAN" 2>/dev/null || echo "0")
    fi
    if [[ $needs_clarification -gt 0 ]]; then
        recommendations+=("Resolve $needs_clarification clarification items in plan")
    fi

    if [[ ${#recommendations[@]} -eq 0 ]]; then
        print_success "No immediate recommendations - memory system is healthy"
    else
        print_info "Suggested improvements:"
        for rec in "${recommendations[@]}"; do
            print_info "  • $rec"
        done
    fi

    echo
}

# MAIN EXECUTION
main() {
    echo -e "${BLUE}YugaStore Memory Banking System Status${NC}"
    echo -e "${BLUE}=====================================${NC}"
    echo

    check_short_term_memory
    check_medium_term_memory
    check_long_term_memory
    calculate_memory_metrics
    provide_recommendations

    print_info "For detailed guidance, see: .specify/memory/memory-banking-guide.md"
}

# Run if executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi