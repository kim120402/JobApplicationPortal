# Presentation Outline

## 1. Introduction

- Project name: Job Portal
- A web application for students/applicants and administrators
- Built as a Spring Boot final project

## 2. Problem/Purpose

- Applicants need a simple way to find active jobs and track applications.
- Administrators need a simple way to manage job postings and review applicants.
- The system keeps the workflow organized in one web application.

## 3. Technologies

- Java 21
- Spring Boot
- Spring MVC and Thymeleaf
- Spring Data JPA
- Spring JDBC with `JdbcTemplate`
- Spring REST
- MySQL
- Maven
- HTML, CSS, and JavaScript

## 4. Architecture

- MVC controllers return Thymeleaf pages.
- REST controllers return JSON DTOs.
- Services contain business rules.
- Spring Data JPA repositories handle normal CRUD.
- `DashboardJdbcRepository` uses `JdbcTemplate` for dashboard statistics.
- `HttpSession` stores the logged-in user.

## 5. Public Workflow

- Home page
- Browse jobs
- Search/filter jobs
- View job details
- Register and login

## 6. Applicant Workflow

- Applicant dashboard
- Apply to active jobs
- Upload PDF or DOCX resume
- Add optional cover letter
- View My Applications
- Track status updates

## 7. Admin Workflow

- Admin dashboard
- Manage jobs
- Create, edit, close, and delete jobs
- View applications
- Open application details
- Download resumes
- Update application status

## 8. Database/JPA/JDBC

- `users`, `jobs`, and `applications` tables
- Applications link users and jobs with foreign keys
- JPA entities use `Long` IDs matching MySQL `BIGINT`
- JDBC is used for the admin dashboard summary query

## 9. REST API

- Public jobs API
- Admin job management API
- Admin application review API
- Applicant-safe application lookup API
- JSON error responses
- Session cookie required for protected routes

## 10. Testing

- Service tests
- MVC controller tests
- REST controller tests
- JDBC repository tests
- Resume upload validation tests
- Latest verified result: 92 tests, 0 failures, 0 errors, 0 skipped

## 11. Conclusion/Future Improvements

- Current project meets the main portal requirements.
- Possible improvements: email notifications, employer accounts, resume preview, and richer applicant profiles.
