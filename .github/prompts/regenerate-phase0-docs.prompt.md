# Regenerate Phase 0 Docs

You are a Product Owner/Business Owner. Analyze this repository as if it were a legacy eCommerce platform (NOT a sample/demo). Ignore any README language that calls it a demo; treat conflicting statements as outdated.

## Goal
Create two business-only documents in the `docs` folder with **NO technical specifications** (no tech stack, frameworks, ports, databases, schemas, APIs/endpoints, latency percentiles, SLO/SLA math, or implementation details). Focus strictly on business purpose, intent, outcomes, scope, stakeholders, risks, governance, and success measures in plain business language.

## CRITICAL OUTPUT/FILE RULES (must follow)
1) **Fully overwrite** the entire contents of each target file. Do not append. Do not preserve any existing text. Replace from first character to last character.
2) Each file must contain **exactly one** complete document (no repeated titles/sections, no leftover draft text, no second copy below).
3) Do **not** include YAML front-matter or metadata blocks. Start each file with a single `#` title line.
4) **Version management:**
   - First, read the existing files to check the current "Document version" in the Versioning section
   - If files exist: increment the minor version (e.g., 1.0 → 1.1, 1.1 → 1.2)
   - If files do not exist: start at version 1.0
   - Update "Last updated" to today's date (use current date in YYYY-MM-DD format)
   - **Preserve all existing entries** in the Change log table and add a new row at the bottom with: today's date, new version number, brief summary of what changed, and owner
   - If this is a full regeneration with content changes, use summary like "Updated [section names] for clarity" or "Refreshed business context and outcomes"
5) After writing, self-check that:
   - Each required section appears **once and only once**.
   - No prohibited technical terms appear.
   - No duplicated headings or repeated content exist.
   - Version number was incremented correctly (if updating existing files)
   - All previous change log entries are preserved

## Deliverables (write exactly these two files)

### A) `docs/projectcharter.md`
Title: **YugaStore — Project Charter**

Must include sections (use these headings verbatim):
- Executive summary
- Business context and purpose (why it exists; who it serves)
- Vision and objectives (success definition)
- In-scope / out-of-scope (business scope only)
- Stakeholders and responsibilities (RACI-lite)
- Core user journeys (customer-facing flows only)
- Operating model (business view, not architecture)
- Success measures (business KPIs only; no technical metrics)
- Assumptions and constraints (business/operational realities only)
- Risks and mitigations (business risk framing)
- Deliverables
- Governance and change control
- Versioning (at the very bottom)

**Versioning must include:** Document version, Status, Owner, Approver, Last updated, Review cadence, plus a Change log table.

### B) `docs/businesspurpose.md`
Title: **YugaStore — Business Purpose**

Must include sections (use these headings verbatim):
- Purpose statement (1–2 paragraphs)
- Business outcomes (customer, commercial, operational) — business phrasing only
- Scope (business-level capabilities)
- Stakeholders
- Constraints and realities (legacy context, business impact)
- How success is measured (business KPIs only)
- Guiding principles
- Versioning (at the very bottom)

**Versioning must include:** Document version, Status, Owner, Approver, Last updated, Review cadence, plus a Change log table.

## Style constraints
- Write like a business owner presenting to leadership and delivery teams.
- Keep each document concise (target 1–2 pages each in Markdown).
- Do not include any code, configuration, URLs, or product/vendor names.
- Do not mention: “microservices”, “Spring”, “React”, or any database terms.
- Ensure the two docs are consistent with each other and do not contradict.

## Output format
Output only the final contents of both files, in two clearly labeled blocks:

1) BEGIN docs/projectcharter.md
...file contents...
END docs/projectcharter.md

2) BEGIN docs/businesspurpose.md
...file contents...
END docs/businesspurpose.md
