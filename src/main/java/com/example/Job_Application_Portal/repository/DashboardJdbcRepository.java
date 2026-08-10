package com.example.Job_Application_Portal.repository;

import com.example.Job_Application_Portal.dto.DashboardStatisticsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public DashboardJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // JdbcTemplate is used here for a compact custom SQL report; CRUD stays in Spring Data JPA.
    public DashboardStatisticsResponse getDashboardStatistics() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM jobs) AS total_jobs,
                    (SELECT COUNT(*) FROM jobs WHERE UPPER(status) = 'ACTIVE') AS active_jobs,
                    (SELECT COUNT(*) FROM jobs WHERE UPPER(status) = 'CLOSED') AS closed_jobs,
                    (SELECT COUNT(*) FROM users WHERE UPPER(role) = 'APPLICANT') AS total_applicants,
                    (SELECT COUNT(*) FROM applications) AS total_applications,
                    (SELECT COUNT(*) FROM applications WHERE UPPER(application_status) = 'UNDER_REVIEW') AS under_review_applications,
                    (SELECT COUNT(*) FROM applications WHERE UPPER(application_status) = 'INTERVIEW') AS interview_applications,
                    (SELECT COUNT(*) FROM applications WHERE UPPER(application_status) = 'HIRED') AS hired_applications
                """;

        return jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> new DashboardStatisticsResponse(
                resultSet.getLong("total_jobs"),
                resultSet.getLong("active_jobs"),
                resultSet.getLong("closed_jobs"),
                resultSet.getLong("total_applicants"),
                resultSet.getLong("total_applications"),
                resultSet.getLong("under_review_applications"),
                resultSet.getLong("interview_applications"),
                resultSet.getLong("hired_applications")
        ));
    }
}
