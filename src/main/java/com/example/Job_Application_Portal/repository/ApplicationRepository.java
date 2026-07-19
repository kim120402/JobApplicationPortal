package com.example.Job_Application_Portal.repository;

import com.example.Job_Application_Portal.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    // Check for duplicate application
    boolean existsByUserUserIdAndJobJobId(
            Long userId,
            Long jobId
    );

    // Get applications submitted by a user
    List<Application> findByUserUserIdOrderByAppliedDateDesc(
            Long userId
    );

    // Get applications for a job
    List<Application> findByJobJobIdOrderByAppliedDateDesc(
            Long jobId
    );

    // Get all applications, newest first
    List<Application> findAllByOrderByAppliedDateDesc();

    // Get applications by status
    List<Application> findByApplicationStatusIgnoreCaseOrderByAppliedDateDesc(
            String applicationStatus
    );

    // Get a user's specific application
    Optional<Application> findByApplicationIdAndUserUserId(
            Long applicationId,
            Long userId
    );

    // Count applications by status
    long countByApplicationStatusIgnoreCase(
            String applicationStatus
    );
}