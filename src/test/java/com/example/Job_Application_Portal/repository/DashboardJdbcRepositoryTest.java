package com.example.Job_Application_Portal.repository;

import com.example.Job_Application_Portal.dto.DashboardStatisticsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class DashboardJdbcRepositoryTest {
    private JdbcTemplate jdbcTemplate;
    private DashboardJdbcRepository dashboardJdbcRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        dashboardJdbcRepository = new DashboardJdbcRepository(jdbcTemplate);
    }

    @Test
    void returnsCorrectDashboardTotals() throws Exception {
        mockDashboardRow(5, 3, 2, 4, 8, 2, 1, 1);

        DashboardStatisticsResponse statistics = dashboardJdbcRepository.getDashboardStatistics();

        assertThat(statistics.totalJobs()).isEqualTo(5);
        assertThat(statistics.activeJobs()).isEqualTo(3);
        assertThat(statistics.closedJobs()).isEqualTo(2);
        assertThat(statistics.totalApplicants()).isEqualTo(4);
        assertThat(statistics.totalApplications()).isEqualTo(8);
        assertThat(statistics.underReviewApplications()).isEqualTo(2);
        assertThat(statistics.interviewApplications()).isEqualTo(1);
        assertThat(statistics.hiredApplications()).isEqualTo(1);
    }

    @Test
    void emptyDatabaseReturnsZeroValues() throws Exception {
        mockDashboardRow(0, 0, 0, 0, 0, 0, 0, 0);

        DashboardStatisticsResponse statistics = dashboardJdbcRepository.getDashboardStatistics();

        assertThat(statistics.totalJobs()).isZero();
        assertThat(statistics.activeJobs()).isZero();
        assertThat(statistics.closedJobs()).isZero();
        assertThat(statistics.totalApplications()).isZero();
    }

    @Test
    void sqlUsesCorrectTableAndColumnNames() throws Exception {
        mockDashboardRow(0, 0, 0, 0, 0, 0, 0, 0);

        dashboardJdbcRepository.getDashboardStatistics();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), any(RowMapper.class));
        String sql = sqlCaptor.getValue();

        assertThat(sql).contains("FROM jobs");
        assertThat(sql).contains("FROM users");
        assertThat(sql).contains("FROM applications");
        assertThat(sql).contains("status");
        assertThat(sql).contains("role");
        assertThat(sql).contains("application_status");
    }

    private void mockDashboardRow(
            long totalJobs,
            long activeJobs,
            long closedJobs,
            long totalApplicants,
            long totalApplications,
            long underReviewApplications,
            long interviewApplications,
            long hiredApplications
    ) throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<DashboardStatisticsResponse> rowMapper = invocation.getArgument(1);
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getLong("total_jobs")).thenReturn(totalJobs);
            when(resultSet.getLong("active_jobs")).thenReturn(activeJobs);
            when(resultSet.getLong("closed_jobs")).thenReturn(closedJobs);
            when(resultSet.getLong("total_applicants")).thenReturn(totalApplicants);
            when(resultSet.getLong("total_applications")).thenReturn(totalApplications);
            when(resultSet.getLong("under_review_applications")).thenReturn(underReviewApplications);
            when(resultSet.getLong("interview_applications")).thenReturn(interviewApplications);
            when(resultSet.getLong("hired_applications")).thenReturn(hiredApplications);
            return rowMapper.mapRow(resultSet, 0);
        });
    }
}
