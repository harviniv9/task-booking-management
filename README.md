# task-booking-management
# Task & Booking Management Application

A Spring Boot–based application for creating, managing, and approving tasks/bookings with a simple approval workflow, role-based access control, and a lightweight HTML/JavaScript UI.

---

## Overview

This application allows users to create tasks or bookings, assign them to other users, and manage approvals through a clear workflow. Managers and admins can approve or reject tasks, while users can track task status through dashboards, lists, and a calendar view.

The project demonstrates clean backend architecture, practical Spring Security usage, and efficient delivery using AI tooling integrated into IntelliJ.

---

## Core Features

### Authentication & Roles
- Session-based form login and logout
- Role-based access:
  - **USER** – create tasks, view tasks, dashboard, and calendar
  - **MANAGER** – all USER actions plus approve/reject tasks
  - **ADMIN** – superset role (USER + MANAGER capabilities)
- Secure session invalidation on logout

---

### Task / Booking Management
- Create tasks with:
  - Title
  - Description
  - Task Date/Time
  - Priority (LOW / MEDIUM / HIGH)
  - Assigned User
- List view of tasks
- Filter by status:
  - PENDING
  - APPROVED
  - REJECTED
- Sort by:
  - Task Date/Time
  - Priority

---

### Approval Workflow
- Status transitions:
  - `PENDING → APPROVED`
  - `PENDING → REJECTED`
- Workflow rules enforced at the **service layer**
- Only MANAGER or ADMIN can approve or reject tasks

---

### Notifications (Email Simulation)
- Console-based notification logging
- Triggered on:
  - Task creation
  - Task approval
  - Task rejection
- Recipients:
  - Assigned user
  - Task creator (if different)
- Designed to be easily replaceable with real email later

---

### Dashboard
- Displays counts of:
  - Pending tasks
  - Approved tasks
  - Rejected tasks
- Shows logged-in user and role

---

### Calendar View (Bonus)
- Monthly calendar view using task date/time
- Tasks displayed as bookings on calendar days
- No backend schema changes required

---

### Export Tasks as CSV (Bonus)
- Export tasks as CSV
- Respects filtering and sorting
- Useful for reporting and auditing

---
### Role-based access control (Admin, Manager, User) (Bonus)
- gave access as mentioned
---
### Unit Tests – Service Layer
- unit tests for service layer

## Technology Stack

### Backend
- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- H2 (in-memory database)

### Frontend
- HTML
- CSS
- Vanilla JavaScript (Fetch API)

### Tooling
- IntelliJ IDEA (Community Edition)
- IntelliJ AI plugin
- Maven
- Postman (optional)

---

## Architecture Overview

Controller → Service → Repository
↓
NotificationService


- Thin controllers
- Business logic enforced in services
- Notifications handled centrally
- Clear separation of concerns

---

## Setup Instructions

### Prerequisites
- Java 17+ (tested with Java 21/22)
- Maven 3.8+
- Git

---

### Clone the Repository

git clone https://github.com/harviniv9/task-booking-management.git
cd task-booking-management


## Build & Run Instructions

### Build the Application

mvn clean install

### Run the Application
mvn spring-boot:run

###The application will start at
http://localhost:8080/login.html

## Default Users (Seeded on Startup)

| Username | Password   | Role    |
|----------|------------|---------|
| user     | user123    | USER    |
| manager  | manager123 | MANAGER |
| admin    | admin123   | ADMIN   |

> **Note:** The application uses an in-memory H2 database. All data resets on restart.

## Application URLs

| Feature | URL |
|--------|-----|
| Login | http://localhost:8080/login.html |
| Dashboard | http://localhost:8080/dashboard.html |
| Tasks | http://localhost:8080/tasks.html |
| Create Task | http://localhost:8080/create-task.html |
| Calendar View | http://localhost:8080/calendar.html |
| H2 Console | http://localhost:8080/h2-console |

---

## H2 Database Configuration

##POST /api/tasks

**Request Body Example**
json 

{
  "title": "Fix login bug",
  "description": "Users cannot login intermittently",
  "taskDateTime": "2026-01-20T10:00:00",
  "priority": "HIGH",
  "assignedUserId": 2
}

## Database (H2 – In-Memory)

The application uses **H2 in-memory database** for simplicity and fast setup.  
No external database installation or configuration is required.
- JDBC URL - jdbc:h2:mem:taskdb
- Username: `SA`
- Password: *(blank)*


### Notes
- All data is lost when the application restarts
- Suitable for development and evaluation purposes
- Can be replaced with PostgreSQL/MySQL by updating configuration only




