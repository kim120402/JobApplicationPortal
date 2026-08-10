# Test Plan

## Automated Tests

Run:

```bash
./mvnw clean test
```

Covered areas:

- User registration and login service rules
- Job service validation, create, update, close, delete
- Application service submission, duplicate prevention, status updates
- Resume upload validation
- Admin MVC controller access and workflow logic
- REST jobs API
- REST applications API
- REST JSON error handling
- JDBC dashboard repository mapping

## Manual Browser Checks

Start MySQL, then run:

```bash
./mvnw spring-boot:run
```

Check:

- `/`
- `/login`
- `/register`
- `/jobs`
- `/applicant/dashboard`
- `/applicant/applications`
- `/admin/dashboard`
- `/admin/jobs`
- `/admin/applications`

## Manual REST Checks

Use `api-tests.http`.

Important:

- Login through `/login`.
- Keep the `JSESSIONID` cookie in the REST client.
- Public job endpoints should work without login.
- Admin endpoints should return `401` without login.
- Admin endpoints should return `403` for applicant sessions.

## Resume Checks

- Upload a PDF resume.
- Upload a DOCX resume.
- Confirm `.exe` is rejected.
- Confirm empty upload is rejected.
- Confirm files over 5 MB are rejected.
- Confirm admin can download resume from application details.
