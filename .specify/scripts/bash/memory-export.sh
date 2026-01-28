#!/usr/bin/env bash

# Memory Banking Export/Import Tool
# Exports memory patterns and knowledge for reuse in other projects

set -e

SCRIPT_DIR="$(CDPATH="" cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

eval $(get_feature_paths)

EXPORT_DIR="$REPO_ROOT/memory-export"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

print_usage() {
    echo "Usage: $0 [export|import] [options]"
    echo ""
    echo "Commands:"
    echo "  export [target_dir]     - Export memory banking artifacts"
    echo "  import [source_dir]     - Import memory banking artifacts"
    echo "  package                 - Create shareable memory package"
    echo "  status                  - Show exportable artifacts"
    echo ""
    echo "Examples:"
    echo "  $0 export ./shared-memory"
    echo "  $0 import ../other-project/memory-export"
    echo "  $0 package"
}

# Export memory artifacts
export_memory() {
    local target_dir="${1:-$EXPORT_DIR}"

    echo "Exporting memory banking artifacts to: $target_dir"

    # Create export directory structure
    mkdir -p "$target_dir"/{constitution,patterns,templates,commands,examples}

    # Export constitution (anonymized)
    if [[ -f "$REPO_ROOT/.specify/memory/constitution.md" ]]; then
        cp "$REPO_ROOT/.specify/memory/constitution.md" "$target_dir/constitution/"
        echo "✓ Exported constitution"
    fi

    # Export patterns library
    if [[ -f "$REPO_ROOT/.specify/memory/patterns-library.md" ]]; then
        cp "$REPO_ROOT/.specify/memory/patterns-library.md" "$target_dir/patterns/"
        echo "✓ Exported patterns library"
    fi

    # Export memory banking guide
    if [[ -f "$REPO_ROOT/.specify/memory/memory-banking-guide.md" ]]; then
        cp "$REPO_ROOT/.specify/memory/memory-banking-guide.md" "$target_dir/patterns/"
        echo "✓ Exported memory banking guide"
    fi

    # Export templates (cleaned)
    if [[ -d "$REPO_ROOT/.specify/templates" ]]; then
        cp -r "$REPO_ROOT/.specify/templates"/* "$target_dir/templates/"
        echo "✓ Exported templates"
    fi

    # Export command patterns
    if [[ -d "$REPO_ROOT/.claude/commands" ]]; then
        cp -r "$REPO_ROOT/.claude/commands"/* "$target_dir/commands/"
        echo "✓ Exported command patterns"
    fi

    # Export example specifications (last 2 features, anonymized)
    if [[ -d "$SPECS_DIR" ]]; then
        local example_count=0
        for spec_dir in $(find "$SPECS_DIR" -mindepth 1 -maxdepth 1 -type d | sort | tail -2); do
            if [[ -f "$spec_dir/spec.md" ]]; then
                local feature_name=$(basename "$spec_dir")
                local example_dir="$target_dir/examples/example-$((++example_count))"
                mkdir -p "$example_dir"

                # Copy and anonymize spec files
                for file in "$spec_dir"/*.md; do
                    if [[ -f "$file" ]]; then
                        local filename=$(basename "$file")
                        # Basic anonymization - remove project-specific details
                        sed -e 's/yugastore/[PROJECT]/g' \
                            -e 's/YugaStore/[PROJECT]/g' \
                            -e 's/ASIN/[PRODUCT_ID]/g' \
                            -e 's/Amazon/[MARKETPLACE]/g' \
                            "$file" > "$example_dir/$filename"
                    fi
                done
                echo "✓ Exported example: $feature_name (anonymized)"
            fi
        done
    fi

    # Create metadata file
    cat > "$target_dir/memory-export-metadata.json" << EOF
{
    "export_timestamp": "$TIMESTAMP",
    "source_project": "$(basename "$REPO_ROOT")",
    "export_version": "1.0",
    "included_artifacts": [
        "constitution",
        "patterns_library",
        "templates",
        "command_patterns",
        "example_specifications"
    ],
    "usage_instructions": "See README.md for import instructions"
}
EOF

    # Create README for the export
    cat > "$target_dir/README.md" << 'EOF'
# Memory Banking Export Package

This package contains reusable memory banking artifacts from an AI-accelerated engineering project.

## Contents

- `constitution/` - Project governance and principles
- `patterns/` - Engineering patterns library and guides
- `templates/` - Reusable specification and planning templates
- `commands/` - AI workflow command patterns
- `examples/` - Anonymized example specifications

## How to Import

1. **Constitution**: Adapt the constitution to your project's needs
2. **Patterns**: Review and adopt relevant engineering patterns
3. **Templates**: Copy templates to your `.specify/templates/` directory
4. **Commands**: Copy commands to your `.claude/commands/` directory
5. **Examples**: Use as reference for writing specifications

## Quick Start

```bash
# Import into new project
./memory-import.sh /path/to/this/export

# Or manually copy what you need
cp templates/* ./.specify/templates/
cp commands/* ./.claude/commands/
cp constitution/constitution.md ./.specify/memory/constitution.md
```

## Customization

- Edit constitution principles for your domain
- Adapt patterns to your technology stack
- Customize templates for your workflow
- Modify commands for your AI toolchain

The memory banking system is designed to evolve with your project!
EOF

    echo ""
    echo "Export complete! Artifacts saved to: $target_dir"
    echo "Package size: $(du -sh "$target_dir" | cut -f1)"
    echo ""
    echo "To share with other projects:"
    echo "  tar -czf memory-banking-export-$TIMESTAMP.tar.gz -C \"$(dirname "$target_dir")\" \"$(basename "$target_dir")\""
}

# Import memory artifacts
import_memory() {
    local source_dir="$1"

    if [[ -z "$source_dir" || ! -d "$source_dir" ]]; then
        echo "Error: Source directory not provided or doesn't exist"
        echo "Usage: $0 import <source_directory>"
        exit 1
    fi

    echo "Importing memory banking artifacts from: $source_dir"

    # Validate source directory
    if [[ ! -f "$source_dir/memory-export-metadata.json" ]]; then
        echo "Warning: Source directory doesn't appear to be a memory export package"
        read -p "Continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 0
        fi
    fi

    # Import constitution (with backup)
    if [[ -f "$source_dir/constitution/constitution.md" ]]; then
        local constitution_target="$REPO_ROOT/.specify/memory/constitution.md"
        if [[ -f "$constitution_target" ]]; then
            cp "$constitution_target" "$constitution_target.backup.$(date +%Y%m%d_%H%M%S)"
            echo "✓ Backed up existing constitution"
        fi

        mkdir -p "$(dirname "$constitution_target")"
        cp "$source_dir/constitution/constitution.md" "$constitution_target"
        echo "✓ Imported constitution"
    fi

    # Import patterns
    if [[ -f "$source_dir/patterns/patterns-library.md" ]]; then
        mkdir -p "$REPO_ROOT/.specify/memory"
        cp "$source_dir/patterns/patterns-library.md" "$REPO_ROOT/.specify/memory/"
        echo "✓ Imported patterns library"
    fi

    if [[ -f "$source_dir/patterns/memory-banking-guide.md" ]]; then
        mkdir -p "$REPO_ROOT/.specify/memory"
        cp "$source_dir/patterns/memory-banking-guide.md" "$REPO_ROOT/.specify/memory/"
        echo "✓ Imported memory banking guide"
    fi

    # Import templates (merge, don't overwrite)
    if [[ -d "$source_dir/templates" ]]; then
        mkdir -p "$REPO_ROOT/.specify/templates"
        for template in "$source_dir/templates"/*; do
            if [[ -f "$template" ]]; then
                local template_name=$(basename "$template")
                local target_template="$REPO_ROOT/.specify/templates/$template_name"

                if [[ -f "$target_template" ]]; then
                    echo "  Template $template_name already exists, skipping"
                else
                    cp "$template" "$target_template"
                    echo "  ✓ Imported template: $template_name"
                fi
            fi
        done
        echo "✓ Imported templates"
    fi

    # Import commands (merge, don't overwrite)
    if [[ -d "$source_dir/commands" ]]; then
        mkdir -p "$REPO_ROOT/.claude/commands"
        for command in "$source_dir/commands"/*; do
            if [[ -f "$command" ]]; then
                local command_name=$(basename "$command")
                local target_command="$REPO_ROOT/.claude/commands/$command_name"

                if [[ -f "$target_command" ]]; then
                    echo "  Command $command_name already exists, skipping"
                else
                    cp "$command" "$target_command"
                    echo "  ✓ Imported command: $command_name"
                fi
            fi
        done
        echo "✓ Imported commands"
    fi

    # Show examples (don't auto-import)
    if [[ -d "$source_dir/examples" ]]; then
        echo "✓ Example specifications available in: $source_dir/examples"
        echo "  Review and manually adapt examples as needed"
    fi

    echo ""
    echo "Import complete! Next steps:"
    echo "1. Review and customize the imported constitution"
    echo "2. Check templates for project-specific customization"
    echo "3. Test AI commands to ensure they work in your environment"
    echo "4. Run: .specify/scripts/bash/memory-status.sh"
}

# Create shareable package
create_package() {
    local package_name="memory-banking-export-$TIMESTAMP.tar.gz"
    local temp_export="/tmp/memory-export-$$"

    echo "Creating shareable memory banking package..."

    # Export to temporary directory
    export_memory "$temp_export"

    # Create compressed package
    tar -czf "$package_name" -C "/tmp" "memory-export-$$"

    # Cleanup
    rm -rf "$temp_export"

    echo "Package created: $package_name"
    echo "Size: $(ls -lh "$package_name" | awk '{print $5}')"
    echo ""
    echo "To share:"
    echo "  scp $package_name user@host:/path/to/destination/"
    echo ""
    echo "To import in another project:"
    echo "  tar -xzf $package_name"
    echo "  ./memory-export.sh import memory-export-$TIMESTAMP"
}

# Show exportable artifacts status
show_status() {
    echo "Memory Banking Export Status"
    echo "============================"
    echo ""

    local exportable_items=0

    # Check constitution
    if [[ -f "$REPO_ROOT/.specify/memory/constitution.md" ]]; then
        local is_template=$(grep -q "\[PROJECT_NAME\]" "$REPO_ROOT/.specify/memory/constitution.md" && echo "template" || echo "customized")
        echo "✓ Constitution: $is_template"
        ((exportable_items++))
    else
        echo "✗ Constitution: missing"
    fi

    # Check patterns
    if [[ -f "$REPO_ROOT/.specify/memory/patterns-library.md" ]]; then
        local pattern_count=$(grep -c "^###" "$REPO_ROOT/.specify/memory/patterns-library.md" || echo "0")
        echo "✓ Patterns Library: $pattern_count patterns"
        ((exportable_items++))
    else
        echo "✗ Patterns Library: missing"
    fi

    # Check templates
    if [[ -d "$REPO_ROOT/.specify/templates" ]]; then
        local template_count=$(find "$REPO_ROOT/.specify/templates" -name "*.md" | wc -l)
        echo "✓ Templates: $template_count files"
        ((exportable_items++))
    else
        echo "✗ Templates: missing"
    fi

    # Check commands
    if [[ -d "$REPO_ROOT/.claude/commands" ]]; then
        local command_count=$(find "$REPO_ROOT/.claude/commands" -name "*.md" | wc -l)
        echo "✓ Commands: $command_count files"
        ((exportable_items++))
    else
        echo "✗ Commands: missing"
    fi

    # Check example specs
    if [[ -d "$SPECS_DIR" ]]; then
        local spec_count=$(find "$SPECS_DIR" -name "spec.md" | wc -l)
        echo "✓ Example Specs: $spec_count features"
        ((exportable_items++))
    else
        echo "✗ Example Specs: missing"
    fi

    echo ""
    echo "Exportable artifacts: $exportable_items/5"

    if [[ $exportable_items -ge 3 ]]; then
        echo "✓ Ready for export"
    else
        echo "✗ Need more artifacts for meaningful export"
    fi
}

# Main execution
main() {
    local command="${1:-status}"

    case "$command" in
        export)
            export_memory "$2"
            ;;
        import)
            import_memory "$2"
            ;;
        package)
            create_package
            ;;
        status)
            show_status
            ;;
        *)
            print_usage
            exit 1
            ;;
    esac
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi