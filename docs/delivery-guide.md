# YugaStore - Delivery Lead Quick Reference Guide

**Version:** 1.0  
**Date:** January 27, 2026  
**Audience:** Delivery Leads, Engineering Managers, Tech Leads

---

## Quick Start

### Essential Information

**System:** Microservices eCommerce platform with 6 services + database  
**Primary Tech:** Java 17, Spring Boot 2.6.3, YugabyteDB, React 16.2  
**Team Size:** 4-5 backend, 2-3 frontend, 1-2 DevOps  
**Typical Sprint Velocity:** 40-50 story points (Medium-sized team)

---

## Service Dependency Map

```
                    YugabyteDB (Must be running first)
                           │
                    Eureka Server (Must start second)
                           │
            ┌──────────────┼──────────────┐
            │              │              │
       Products         Cart          Checkout
       (8082)          (8083)         (8086)
            │              │              │
            └──────────────┼──────────────┘
                           │
                    API Gateway (8081)
                           │
                      React UI (8080)
```

**Critical Path:** YugabyteDB → Eureka → Backend Services → API Gateway → UI

**Startup Time:** ~3-5 minutes for all services

---

## Effort Estimation Guide

### By Change Type

| Change Type | Effort | Example | Risk |
|-------------|--------|---------|------|
| UI-only change | 1-2 days | Update button color, add tooltip | Low |
| Service logic change | 2-3 days | Add validation rule, update calculation | Low-Medium |
| New API endpoint | 3-5 days | Add new product filter | Medium |
| Database schema change | 1 week | Add new column, index | Medium-High |
| New microservice feature | 1-2 weeks | Add product reviews | High |
| Cross-service feature | 2-3 weeks | Multi-service workflow change | High |
| New service | 3-4 weeks | New microservice from scratch | Very High |

### Complexity Multipliers

- **Add 50% if:** Requires YCQL (Cassandra) changes
- **Add 30% if:** Involves transaction handling
- **Add 40% if:** Requires Feign client changes
- **Add 25% if:** Needs extensive testing (checkout flow)
- **Add 20% if:** Team unfamiliar with technology

### Story Point Reference

- **1 point:** Config change, minor bug fix (2-4 hours)
- **2 points:** Simple service logic, UI component (4-8 hours)
- **3 points:** API endpoint, service integration (1-2 days)
- **5 points:** Feature with testing, docs (2-3 days)
- **8 points:** Complex feature, multi-service (1 week)
- **13 points:** Major feature, architecture change (2 weeks)
- **20+ points:** Epic, break down further

---

## Pre-Planning Checklist

Before committing to delivery, verify:

**Requirements:**
- [ ] Acceptance criteria clear and testable
- [ ] Data model impact understood
- [ ] UI mockups available (if UI changes)
- [ ] Business logic documented
- [ ] Edge cases identified

**Technical:**
- [ ] Service(s) affected identified
- [ ] Database changes required (YSQL/YCQL)
- [ ] API contracts defined
- [ ] Integration points mapped
- [ ] Performance impact assessed

**Dependencies:**
- [ ] External dependencies identified
- [ ] Team member availability confirmed
- [ ] Required skills present on team
- [ ] Infrastructure needs identified
- [ ] Third-party service dependencies

**Testing:**
- [ ] Test data requirements defined
- [ ] Test environment needs identified
- [ ] Performance testing needed?
- [ ] Manual testing scenarios defined
- [ ] Rollback plan considered

---

## Common Development Tasks

### Task: Add New Product Attribute

**Effort:** 3-5 days  
**Services Affected:** Products, API Gateway, React UI  
**Complexity:** Medium  

**Steps:**
1. Update YCQL schema (add column to products table) - 2 hours
2. Update Product domain model - 1 hour
3. Update ProductService to populate new field - 2 hours
4. Update API Gateway response mapping - 1 hour
5. Update React UI to display attribute - 4 hours
6. Add unit tests - 4 hours
7. Integration testing - 4 hours
8. Documentation - 2 hours

**Risks:**
- YCQL schema changes cannot be easily rolled back
- May need data migration for existing products
- UI layout may need adjustment

---

### Task: Add Product Filter/Search

**Effort:** 1-2 weeks  
**Services Affected:** Products, API Gateway, React UI  
**Complexity:** High  

**Steps:**
1. Design YCQL secondary index strategy - 4 hours
2. Create indexes in YugabyteDB - 2 hours
3. Implement filter logic in ProductService - 1-2 days
4. Add new API endpoint in Products service - 4 hours
5. Update API Gateway with new endpoint - 3 hours
6. Implement UI filter components - 2-3 days
7. Add pagination support - 1 day
8. Unit tests (all layers) - 2 days
9. Integration tests - 1 day
10. Performance testing - 1 day

**Risks:**
- Index creation on large dataset may take time
- Query performance may require tuning
- Complex filter combinations increase complexity

---

### Task: Implement User Authentication

**Effort:** 3-4 weeks  
**Services Affected:** Login, API Gateway, Cart, Checkout, React UI  
**Complexity:** Very High  

**Steps:**
1. Complete Login service implementation - 1 week
2. Add JWT token generation and validation - 3-4 days
3. Integrate Spring Security in API Gateway - 3-4 days
4. Update Cart to use real user IDs - 2-3 days
5. Update Checkout to use real user IDs - 2-3 days
6. Add login/registration UI - 1 week
7. Add session management - 2-3 days
8. Security testing - 3-4 days
9. Update all existing tests - 2-3 days

**Risks:**
- Major architectural change
- Impacts all services
- Requires security review
- High regression testing needs
- Session management complexity

---

## Sprint Planning Tips

### Velocity Calculation

**New Team (Sprint 1-3):**
- Conservative: 20-30 points per sprint
- Allow time for setup and learning
- Plan for unexpected issues

**Established Team (Sprint 4+):**
- Typical: 40-50 points per sprint
- 20-30% buffer for bugs and support
- 15-20% for technical debt

**Team Capacity:**
- 5 backend engineers × 6 productive hours × 10 days = 300 hours
- 2 frontend engineers × 6 productive hours × 10 days = 120 hours
- Account for: meetings (20%), code review (10%), support (15%)
- Effective capacity: ~235 hours backend, ~100 hours frontend

### Work Breakdown

**Day 1-2 (Planning):**
- Story refinement with product owner
- Technical design discussions
- Task breakdown
- Risk identification
- Sprint commitment

**Day 3-8 (Development):**
- Feature development
- Unit tests
- Code reviews
- Integration work
- Daily standups

**Day 9 (Testing/Hardening):**
- Integration testing
- Bug fixes
- Documentation updates
- Demo preparation

**Day 10 (Sprint Close):**
- Sprint demo
- Retrospective
- Next sprint planning prep

---

## Quality Gates

### Before Starting Development
- [ ] Story points estimated by team
- [ ] Technical approach agreed
- [ ] Dependencies identified
- [ ] Acceptance criteria reviewed
- [ ] Test plan outlined

### Before Code Review
- [ ] Unit tests written and passing
- [ ] Code follows style guidelines
- [ ] No obvious bugs or issues
- [ ] Self-review completed
- [ ] Documentation updated

### Before Merge
- [ ] Code reviewed and approved
- [ ] All tests passing in CI
- [ ] No merge conflicts
- [ ] Integration points verified
- [ ] Breaking changes documented

### Before Release
- [ ] All acceptance criteria met
- [ ] Integration tests passing
- [ ] Performance acceptable
- [ ] Documentation complete
- [ ] Rollback plan documented
- [ ] Business stakeholder approval

---

## Risk Assessment Matrix

### Pre-Sprint Risk Evaluation

For each story, assess:

**Technical Risk:**
- Low: Well-understood tech, similar to past work
- Medium: Some unknowns, moderate complexity
- High: New technology, complex integration

**Business Risk:**
- Low: Non-customer-facing, easy rollback
- Medium: Customer-facing, controlled rollout possible
- High: Critical flow (checkout), revenue impact

**Action Based on Risk:**
- **Low/Low:** Proceed with standard process
- **Low/Medium:** Add extra testing
- **Medium/Low:** Add technical spike/proof of concept
- **Medium/Medium:** Increase code review rigor, staging validation
- **High/Any or Any/High:** Add architectural review, phased rollout, feature flag

---

## Daily Standup Template

**For Each Team Member:**

1. **Yesterday:** What did you complete?
2. **Today:** What will you work on?
3. **Blockers:** What's preventing progress?
4. **Risks:** Any concerns about hitting sprint goal?

**Delivery Lead Focus:**
- Track progress toward sprint goal
- Identify impediments early
- Adjust plan if needed
- Escalate risks immediately

**Red Flags:**
- Same blocker multiple days
- Story not progressing
- Unclear scope or acceptance criteria
- Dependencies not resolved
- Team member overwhelmed

---

## Common Blockers & Solutions

### Blocker: "Waiting for Eureka to start"
- **Impact:** Cannot test service integration
- **Solution:** Run Eureka in background during development
- **Prevention:** Docker compose setup with Eureka

### Blocker: "YugabyteDB connection issues"
- **Impact:** Cannot develop or test
- **Solution:** Check YugabyteDB status, restart if needed
- **Prevention:** Health check script, auto-restart

### Blocker: "Service not finding other services"
- **Impact:** Integration broken
- **Solution:** Verify Eureka registration, check service names
- **Prevention:** Consistent naming convention, startup scripts

### Blocker: "Slow YCQL queries"
- **Impact:** Feature performance unacceptable
- **Solution:** Add index, optimize query, use pagination
- **Prevention:** Query planning during design

### Blocker: "Unclear requirements"
- **Impact:** Cannot proceed with development
- **Solution:** Schedule immediate clarification with product owner
- **Prevention:** Better refinement process, written acceptance criteria

### Blocker: "Merge conflicts"
- **Impact:** Cannot merge PR
- **Solution:** Rebase on main, resolve conflicts, re-test
- **Prevention:** Smaller PRs, frequent merges, feature flags

### Blocker: "Test environment down"
- **Impact:** Cannot complete integration testing
- **Solution:** Restore test environment, have backup plan
- **Prevention:** Automated environment setup, monitoring

---

## Deployment Readiness Checklist

### 1 Week Before Deploy

- [ ] Feature complete and code frozen
- [ ] All tests passing
- [ ] Performance tested and acceptable
- [ ] Security review (if needed)
- [ ] Deployment runbook updated
- [ ] Rollback procedure tested
- [ ] Communication plan ready

### 1 Day Before Deploy

- [ ] Final code review completed
- [ ] Staging deployment successful
- [ ] Smoke tests passed in staging
- [ ] Backup completed
- [ ] On-call engineer assigned
- [ ] Stakeholders notified of deployment window

### Day of Deploy

- [ ] Pre-deployment checklist completed
- [ ] Team members available
- [ ] Monitoring dashboards open
- [ ] Communication channels ready
- [ ] Rollback plan accessible

### Post-Deploy (First 2 Hours)

- [ ] All services healthy
- [ ] Error rates normal
- [ ] Performance within bounds
- [ ] Key user flows tested
- [ ] Business metrics validated
- [ ] Stakeholders notified of success

---

## Incident Response Quick Reference

### Severity Determination

**P0 (Critical) - All Hands:**
- Complete system outage
- Checkout broken
- Data corruption
- Response: Immediate

**P1 (High) - Primary On-Call:**
- Major feature down
- Severe performance degradation
- High error rates
- Response: <30 minutes

**P2 (Medium) - Next Business Hours:**
- Minor feature issues
- Moderate performance impact
- Low error rates
- Response: <2 hours

**P3 (Low) - Backlog:**
- Cosmetic issues
- No functional impact
- Response: Next sprint

### Incident Response Steps

1. **Acknowledge** (1 minute)
   - Respond to alert
   - Assign incident commander

2. **Assess** (5 minutes)
   - Determine severity
   - Identify affected services
   - Estimate customer impact

3. **Communicate** (2 minutes)
   - Update status page
   - Notify stakeholders
   - Post in incident channel

4. **Mitigate** (30-60 minutes)
   - Apply quick fix or rollback
   - Restore service
   - Verify resolution

5. **Monitor** (2 hours)
   - Watch for recurrence
   - Track metrics
   - Update stakeholders

6. **Post-Mortem** (1-2 days)
   - Root cause analysis
   - Document timeline
   - Action items for prevention

### Rollback Decision Tree

```
Is critical functionality broken?
├─ Yes → Rollback immediately
└─ No
    ├─ Are error rates >5%?
    │  ├─ Yes → Rollback
    │  └─ No → Continue
    └─ Is performance degraded >3x?
       ├─ Yes → Rollback
       └─ No → Monitor and fix forward
```

---

## Metrics to Track

### Delivery Metrics

**Sprint Metrics:**
- Velocity (story points completed)
- Commitment accuracy (% of committed work delivered)
- Spillover rate (stories moved to next sprint)
- Bug escape rate (bugs found in production)

**Cycle Time:**
- Coding time (story start → PR open)
- Review time (PR open → PR approved)
- Deployment time (PR merged → production)
- Total cycle time (story start → production)

**Quality Metrics:**
- Code coverage percentage
- Test pass rate
- Production bug rate
- Rollback frequency

### System Metrics

**Availability:**
- Uptime percentage (target: 99.9%)
- Mean time to recovery (MTTR)
- Mean time between failures (MTBF)

**Performance:**
- API response time (p50, p95, p99)
- Database query latency
- Throughput (requests/second)

**Business Metrics:**
- Checkout success rate
- Cart operations success rate
- Page load times

---

## Contact Information

**Escalation Path:**

1. **Delivery Lead** → Technical decisions, sprint issues
2. **Product Owner** → Business priorities, requirements
3. **DevOps Lead** → Infrastructure, deployments
4. **Executive Sponsor** → Strategic decisions, conflicts

**Emergency Contacts:**
- On-Call Engineer: [Pager/Phone]
- DevOps Lead: [Contact]
- Product Owner: [Contact]
- Executive Sponsor: [Contact]

---

## Additional Resources

- **Full Architecture:** [architecture.md](architecture.md)
- **Architecture Diagrams:** [architecture-diagram.txt](architecture-diagram.txt)
- **Business Requirements:** [business-requirements.md](business-requirements.md)
- **README:** [../README.md](../README.md)

---

**Document Version:** 1.0  
**Last Updated:** January 27, 2026  
**Next Review:** Quarterly or after major architecture changes

---

*This guide is a living document. Update based on team retrospectives and lessons learned.*
