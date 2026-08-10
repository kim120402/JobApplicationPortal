package com.example.Job_Application_Portal.repository;

import com.example.Job_Application_Portal.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // Display active or closed jobs, with newest jobs first
    List<Job> findByStatusIgnoreCaseOrderByPostedDateDesc(
            String status
    );

    List<Job> findAllByOrderByPostedDateDesc();

    long countByStatusIgnoreCase(
            String status
    );

    // Search active jobs by title
    List<Job> findByTitleContainingIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
            String title,
            String status
    );

    // Search active jobs by location
    List<Job> findByLocationContainingIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
            String location,
            String status
    );

    // Filter active jobs by category
    List<Job> findByCategoryIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
            String category,
            String status
    );

    // Filter active jobs by employment type
    List<Job> findByEmploymentTypeIgnoreCaseAndStatusIgnoreCaseOrderByPostedDateDesc(
            String employmentType,
            String status
    );
}
