package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.dto.DashboardStatisticsResponse;
import com.example.Job_Application_Portal.repository.DashboardJdbcRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final DashboardJdbcRepository dashboardJdbcRepository;

    public DashboardService(DashboardJdbcRepository dashboardJdbcRepository) {
        this.dashboardJdbcRepository = dashboardJdbcRepository;
    }

    public DashboardStatisticsResponse getDashboardStatistics() {
        return dashboardJdbcRepository.getDashboardStatistics();
    }
}
