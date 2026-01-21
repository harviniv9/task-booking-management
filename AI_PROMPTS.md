# AI Prompt Log

---

## Architecture & Planning

- I’m building a Spring Boot task/booking manager with a simple approval workflow. Give me a clean architecture that stays minimal: entities, services, repositories, controllers.
- Break the requirements into a step-by-step implementation plan. Mark what’s core vs optional.
- Double-check the plan for workflow rules, edge cases, status transitions, and role boundaries.

---

## Authentication & Security

- For this app, is session-based form login enough, or should I do JWT/OAuth? Recommend the simplest correct approach.
- I want both browser UI and Postman API testing. What’s the cleanest security design so APIs return JSON errors instead of HTML redirects?
- Give me a minimal Spring Security configuration that supports form login/logout, roles USER/MANAGER/ADMIN, and API access.

---

## Debugging & Fixes

- I’m getting redirected to /login when I hit /h2-console. What changes do I need to allow H2 console access safely in dev?
- Spring shows a generated password in logs. Why can’t I log in with my DB users yet?
- I’m seeing: “Encoded password does not look like BCrypt.” What’s causing this and how do I fix it?

---

## API & Workflow

- Validate my entity design (User, Task). Anything missing for the approval workflow?
- Confirm approval rules: only PENDING can be approved/rejected; only MANAGER can approve/reject.
- Review task creation, listing, and approval endpoints for correctness and minimal scope.

---

## UI & UX

- Generate simple HTML + vanilla JS pages for login, dashboard, tasks list, and create task form.
- Diagnose why login succeeds but redirect to dashboard fails.
- Implement filtering and sorting in the tasks UI.
- Redesign all application pages with a consistent, modern enterprise UI.
- Use a soft gradient background, glassmorphism-style cards, rounded corners, subtle shadows, and clear visual hierarchy.
- Ensure typography is readable, spacing is balanced, and the UI feels lightweight yet professional.
- The design should work well for dashboards and internal tools rather than marketing websites.
- Redesign the dashboard to provide an at-a-glance overview of tasks.
- Use statistic cards for Pending, Approved, and Rejected tasks with visual distinction.

---

## Enhancements

- Implement console notification logs for task creation, approval, and rejection.
- Refactor notification logic into a dedicated NotificationService.
- Implement CSV export for tasks respecting filtering and sorting.
- Propose and implement a lightweight calendar view using existing taskDateTime.

---
