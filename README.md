# Job Portal

Group Members: Kim Joson, Zixin Li
Professor Name: Khuram Khalid  
Course/Section: Web Application Development 
Presentation Date: August 10, 2026

## Project Description

Job Portal is a Spring Boot web application for job browsing, applicant registration, resume-based applications, application status tracking, and administrator job/application management.

The project uses simple `HttpSession` authentication and keeps the original package name:

```text
com.example.Job_Application_Portal
```

The MySQL database name is:

```text
jobportal_db
```

## Technology Stack

- Java 21
- Spring Boot
- Spring MVC and Thymeleaf
- Spring Data JPA
- Spring JDBC with `JdbcTemplate`
- Spring REST controllers
- MySQL
- Maven
- HTML, and CSS
- Multipart resume upload
- BCrypt password hashing through `spring-security-crypto`

## Features

Public users can view the home page, browse active jobs, search jobs, view job details, register, and log in.

Applicants can use the applicant dashboard, apply to active jobs, upload PDF or DOCX resumes, add an optional cover letter, view their own application history, and track status changes.

Administrators can use the admin dashboard, view JdbcTemplate statistics, create/edit/close/delete jobs, view all applications, download resumes, and update application statuses.

REST endpoints provide JSON access for jobs, applications, and admin dashboard statistics with session-based authorization and DTO responses.

## Architecture

- `controller`: Spring MVC controllers for Thymeleaf pages.
- `rest`: REST controllers and REST error handling.
- `service`: Business rules for users, jobs, applications, resumes, and dashboard statistics.
- `repository`: Spring Data JPA repositories plus `DashboardJdbcRepository`.
- `model`: JPA entities for `User`, `Job`, and `Application`.
- `dto`: REST request and response DTOs.
- `templates`: Thymeleaf pages.
- `static/css/style.css`: custom UI styling.

Normal CRUD uses Spring Data JPA. The admin dashboard statistics report uses `JdbcTemplate` to demonstrate Spring JDBC.

## Setup

1. Install Java 21.
2. Install and start MySQL.
3. Create the database and tables:

```powershell
mysql -u root -p < database/schema.sql
```

4. Optional: load demo/sample data:

```powershell
mysql -u root -p < database/sample-data.sql
```

5. Review `src/main/resources/application.properties`.

Default local configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobportal_db
spring.datasource.username=root
spring.datasource.password=
app.upload.dir=uploads/resumes
```

Use `src/main/resources/application-example.properties` as a reference if your MySQL username/password differs.

## Demo Accounts

For local classroom/demo use only, `database/sample-data.sql` includes:

| Role | Email | Password |
| --- | --- | --- |
| ADMIN | `admin@example.com` | `Admin123!` |
| APPLICANT | `applicant@example.com` | `Applicant123!` |

The SQL stores BCrypt hashes, not plain-text passwords.

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

Open:

```text
http://localhost:8080
```

MySQL must be running before the application starts.

## Test

```powershell
.\mvnw.cmd clean test
```

Latest verified Surefire result:

```text
Tests run: 92
Failures: 0
Errors: 0
Skipped: 0
```

## Main MVC Routes

| Route | Access | Purpose |
| --- | --- | --- |
| `/` | Public | Home page |
| `/jobs` | Public | Browse active jobs |
| `/jobs/search` | Public | Search/filter active jobs |
| `/jobs/{jobId}` | Public | Job details |
| `/register` | Public | Applicant registration |
| `/login` | Public | Login |
| `/logout` | Logged in | Logout via POST |
| `/applicant/dashboard` | Applicant | Applicant dashboard |
| `/jobs/{jobId}/apply` | Applicant | Apply to a job |
| `/applicant/applications` | Applicant | Own applications |
| `/admin/dashboard` | Admin | Admin dashboard |
| `/admin/jobs` | Admin | Manage jobs |
| `/admin/jobs/new` | Admin | Create job |
| `/admin/jobs/{jobId}/edit` | Admin | Edit job |
| `/admin/jobs/{jobId}/delete` | Admin | Delete job via POST |
| `/admin/applications` | Admin | View all applications |
| `/admin/applications/{applicationId}` | Admin | Application details |
| `/admin/applications/{applicationId}/resume` | Admin | Download resume |
| `/admin/applications/{applicationId}/status` | Admin | Update application status |

## REST API Routes

| Route | Access | Purpose |
| --- | --- | --- |
| `GET /api/jobs` | Public | List jobs with optional filters |
| `GET /api/jobs/{jobId}` | Public | Get one job |
| `POST /api/jobs` | Admin | Create job |
| `PUT /api/jobs/{jobId}` | Admin | Update job |
| `DELETE /api/jobs/{jobId}` | Admin | Delete job if no applications exist |
| `GET /api/applications` | Admin | List applications |
| `GET /api/applications/{applicationId}` | Admin or owning applicant | View one application |
| `GET /api/applications/user/{userId}` | Admin or same applicant | View applications by user |
| `PUT /api/applications/{applicationId}/status` | Admin | Update application status |
| `GET /api/admin/statistics` | Admin | JDBC dashboard statistics |

Manual REST examples are in `api-tests.http`. A REST client must keep the `JSESSIONID` cookie after login for protected routes.

## Resume Upload Rules

- Accepted formats: PDF and DOCX.
- Empty files are rejected.
- Files over 5 MB are rejected.
- Files are saved under the configured upload directory, default `uploads/resumes`.
- Saved filenames are UUID-based.
- MySQL stores only the generated filename.
- MVC pages and REST responses do not expose physical server paths.

## Database Information

`database/schema.sql` defines:

- `users` with `user_id BIGINT`
- `jobs` with `job_id BIGINT`
- `applications` with `application_id BIGINT`, `user_id BIGINT`, and `job_id BIGINT`

These match the Java entity ID type `Long`.

Foreign keys use `ON DELETE RESTRICT` so users and jobs are not accidentally removed when applications reference them.

## Demo Instructions

1. Start MySQL.
2. Apply `database/schema.sql`.
3. Load `database/sample-data.sql`.
4. Start the application.
5. Log in as the demo admin and show admin dashboard, jobs, applications, resume download, and status update.
6. Log in or register as an applicant and show browsing, applying, and My Applications.
7. Use `api-tests.http` to show REST JSON responses.

## Notes

- The project intentionally does not use Spring Security login configuration.
- Authentication is session-based using `loggedInUser`.
- Application statuses are `APPLIED`, `UNDER_REVIEW`, `INTERVIEW`, `REJECTED`, and `HIRED`.
- Job statuses are `ACTIVE` and `CLOSED`.
