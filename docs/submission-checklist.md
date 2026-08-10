# Submission Checklist

## Code

- [x] Java package remains `com.example.Job_Application_Portal`.
- [x] Database name remains `jobportal_db`.
- [x] Spring MVC pages are implemented.
- [x] Applicant workflow code is implemented.
- [x] Admin workflow code is implemented.
- [x] REST API endpoints are implemented.
- [x] Spring JDBC dashboard statistics are implemented.
- [x] Centralized REST error handling is implemented.
- [x] Resume upload validation is implemented.
- [x] MySQL schema scripts are included.
- [x] Manual API test file is included.
- [x] Automated tests pass with `.\mvnw.cmd clean test`.

## Manual Verification

- [ ] MySQL service is running locally.
- [ ] `database/schema.sql` has been applied.
- [ ] `database/sample-data.sql` has been loaded.
- [ ] `.\mvnw.cmd spring-boot:run` starts successfully with MySQL running.
- [ ] Browser workflow checked from Home through applicant application submission.
- [ ] Browser workflow checked from admin login through status update.
- [ ] REST routes manually checked with a retained session cookie.
- [ ] Resume upload manually checked.
- [ ] Resume download manually checked as admin.
- [ ] Screenshots captured and saved in `screenshots/`.

## Submission Placeholders

- [ ] Add Student Name: [STUDENT NAME].
- [ ] Add Professor Name: [PROFESSOR NAME].
- [ ] Add Course/Section: [COURSE/SECTION].
- [ ] Add Presentation Date: [PRESENTATION DATE].
- [ ] Review final README.
