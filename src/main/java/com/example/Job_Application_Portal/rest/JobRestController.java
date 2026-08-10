package com.example.Job_Application_Portal.rest;

import com.example.Job_Application_Portal.dto.JobRequest;
import com.example.Job_Application_Portal.dto.JobResponse;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.service.DashboardService;
import com.example.Job_Application_Portal.service.JobService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class JobRestController {
    private final JobService jobService;
    private final SessionAccessHelper sessionAccessHelper;
    private final DashboardService dashboardService;

    public JobRestController(
            JobService jobService,
            SessionAccessHelper sessionAccessHelper,
            DashboardService dashboardService
    ) {
        this.jobService = jobService;
        this.sessionAccessHelper = sessionAccessHelper;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/jobs")
    public List<JobResponse> getJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String employmentType
    ) {
        return jobService.getAllJobs().stream()
                .filter(job -> equalsIgnoreCase(job.getStatus(), status))
                .filter(job -> containsIgnoreCase(job.getTitle(), title))
                .filter(job -> containsIgnoreCase(job.getLocation(), location))
                .filter(job -> equalsIgnoreCase(job.getCategory(), category))
                .filter(job -> equalsIgnoreCase(job.getEmploymentType(), employmentType))
                .map(JobResponse::from)
                .toList();
    }

    @GetMapping("/jobs/{jobId}")
    public JobResponse getJob(@PathVariable Long jobId) {
        return JobResponse.from(jobService.getJobById(jobId));
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            HttpSession session
    ) {
        sessionAccessHelper.requireAdmin(session);
        Job createdJob = jobService.createJob(toJob(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{jobId}")
                .buildAndExpand(createdJob.getJobId())
                .toUri();

        return ResponseEntity.created(location).body(JobResponse.from(createdJob));
    }

    @PutMapping("/jobs/{jobId}")
    public JobResponse updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request,
            HttpSession session
    ) {
        sessionAccessHelper.requireAdmin(session);
        Job job = toJob(request);
        job.setPostedDate(null);
        return JobResponse.from(jobService.updateJob(jobId, job));
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId, HttpSession session) {
        sessionAccessHelper.requireAdmin(session);
        jobService.deleteJob(jobId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/admin/statistics")
    public Object statistics(HttpSession session) {
        sessionAccessHelper.requireAdmin(session);
        return dashboardService.getDashboardStatistics();
    }

    private Job toJob(JobRequest request) {
        Job job = new Job();
        job.setTitle(request.title());
        job.setCompanyName(request.companyName());
        job.setLocation(request.location());
        job.setCategory(request.category());
        job.setEmploymentType(request.employmentType());
        job.setDescription(request.description());
        job.setRequirements(request.requirements());
        job.setStatus(request.status());
        return job;
    }

    private boolean containsIgnoreCase(String value, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        return value != null && value.toLowerCase().contains(searchTerm.trim().toLowerCase());
    }

    private boolean equalsIgnoreCase(String value, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        return value != null && value.equalsIgnoreCase(searchTerm.trim());
    }
}
