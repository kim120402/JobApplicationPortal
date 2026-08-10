package com.example.Job_Application_Portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications") // tell JPA that this class represent database table
public class Application {

    // This is primary key and generate ID automatically
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    // Many applications can belong to one user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many applications can belong to one job
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Store resume filename. Required and 255 character max
    @Column(name = "resume_file_name", nullable = false, length = 255)
    private String resumeFileName;

    // TEXT is used because a cover letter can be much longer than a normal short string
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    // Every application must have status and 50 max character
    // APPLIED, UNDER_REVIEW, INTERVIEW, REJECTED, HIRED
    @Column(name = "application_status", nullable = false, length = 50)
    private String applicationStatus;

    // stores the date and time when the applicant submitted the application
    @Column(name = "applied_date", nullable = false)
    private LocalDateTime appliedDate;

    public Application() {
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }
}
