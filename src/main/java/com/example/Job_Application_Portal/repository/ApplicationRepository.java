package com.example.Job_Application_Portal.repository;

import com.example.Job_Application_Portal.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    // Check for duplicate application
    boolean existsByUserUserIdAndJobJobId(
            Long userId,
            Long jobId
    );

    boolean existsByJobJobId(
            Long jobId
    );

    // Get applications submitted by a user
    List<Application> findByUserUserIdOrderByAppliedDateDesc(
            Long userId
    );

    @Query("""
            SELECT a
            FROM Application a
            LEFT JOIN FETCH a.user
            LEFT JOIN FETCH a.job
            WHERE a.user.userId = :userId
            ORDER BY a.appliedDate DESC
            """)
    List<Application> findByUserUserIdWithUserAndJobOrderByAppliedDateDesc(
            Long userId
    );

    long countByUserUserId(
            Long userId
    );

    long countByUserUserIdAndApplicationStatusIgnoreCase(
            Long userId,
            String applicationStatus
    );

    // Get applications for a job
    List<Application> findByJobJobIdOrderByAppliedDateDesc(
            Long jobId
    );

    @Query("""
            SELECT a
            FROM Application a
            LEFT JOIN FETCH a.user
            LEFT JOIN FETCH a.job
            WHERE a.job.jobId = :jobId
            ORDER BY a.appliedDate DESC
            """)
    List<Application> findByJobJobIdWithUserAndJobOrderByAppliedDateDesc(
            Long jobId
    );

    // Get all applications, newest first
    List<Application> findAllByOrderByAppliedDateDesc();

    @Query("""
            SELECT a
            FROM Application a
            LEFT JOIN FETCH a.user
            LEFT JOIN FETCH a.job
            ORDER BY a.appliedDate DESC
            """)
    List<Application> findAllWithUserAndJobOrderByAppliedDateDesc();

    // Get applications by status
    List<Application> findByApplicationStatusIgnoreCaseOrderByAppliedDateDesc(
            String applicationStatus
    );

    @Query("""
            SELECT a
            FROM Application a
            LEFT JOIN FETCH a.user
            LEFT JOIN FETCH a.job
            WHERE UPPER(a.applicationStatus) = UPPER(:applicationStatus)
            ORDER BY a.appliedDate DESC
            """)
    List<Application> findByApplicationStatusIgnoreCaseWithUserAndJobOrderByAppliedDateDesc(
            String applicationStatus
    );

    @Query("""
            SELECT a
            FROM Application a
            LEFT JOIN FETCH a.user
            LEFT JOIN FETCH a.job
            WHERE a.applicationId = :applicationId
            """)
    Optional<Application> findByIdWithUserAndJob(
            Long applicationId
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
