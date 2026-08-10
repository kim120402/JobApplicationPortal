package com.example.Job_Application_Portal.rest;

import com.example.Job_Application_Portal.dto.DashboardStatisticsResponse;
import com.example.Job_Application_Portal.exception.ForbiddenAccessException;
import com.example.Job_Application_Portal.exception.JobDeletionBlockedException;
import com.example.Job_Application_Portal.exception.ResourceNotFoundException;
import com.example.Job_Application_Portal.exception.UnauthorizedAccessException;
import com.example.Job_Application_Portal.model.Job;
import com.example.Job_Application_Portal.service.DashboardService;
import com.example.Job_Application_Portal.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobRestControllerTest {
    private JobService jobService;
    private SessionAccessHelper sessionAccessHelper;
    private DashboardService dashboardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jobService = mock(JobService.class);
        sessionAccessHelper = mock(SessionAccessHelper.class);
        dashboardService = mock(DashboardService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new JobRestController(jobService, sessionAccessHelper, dashboardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getJobsReturnsOk() throws Exception {
        when(jobService.getAllJobs()).thenReturn(List.of(job(1L, "Developer", "ACTIVE")));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Developer"))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void getJobReturnsSelectedJob() throws Exception {
        when(jobService.getJobById(1L)).thenReturn(job(1L, "Developer", "ACTIVE"));

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1));
    }

    @Test
    void missingJobReturnsNotFoundJson() throws Exception {
        when(jobService.getJobById(99L)).thenThrow(new ResourceNotFoundException("Job not found"));

        mockMvc.perform(get("/api/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job not found"))
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    void searchFiltersWork() throws Exception {
        when(jobService.getAllJobs()).thenReturn(List.of(
                job(1L, "Developer", "ACTIVE"),
                job(2L, "Assistant", "CLOSED")
        ));

        mockMvc.perform(get("/api/jobs?status=ACTIVE&title=dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Developer"));
    }

    @Test
    void unauthenticatedPostReturnsUnauthorized() throws Exception {
        when(sessionAccessHelper.requireAdmin(any())).thenThrow(new UnauthorizedAccessException("Login is required"));

        mockMvc.perform(post("/api/jobs").contentType("application/json").content(validJobJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applicantPostReturnsForbidden() throws Exception {
        when(sessionAccessHelper.requireAdmin(any())).thenThrow(new ForbiddenAccessException("Admin access is required"));

        mockMvc.perform(post("/api/jobs").contentType("application/json").content(validJobJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPostCreatesJob() throws Exception {
        when(jobService.createJob(any(Job.class))).thenReturn(job(3L, "Developer", "ACTIVE"));

        mockMvc.perform(post("/api/jobs").contentType("application/json").content(validJobJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/jobs/3")))
                .andExpect(jsonPath("$.jobId").value(3));
    }

    @Test
    void invalidJobReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/jobs").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void adminPutUpdatesExistingJob() throws Exception {
        when(jobService.updateJob(any(), any(Job.class))).thenReturn(job(1L, "Developer", "ACTIVE"));

        mockMvc.perform(put("/api/jobs/1").contentType("application/json").content(validJobJson()))
                .andExpect(status().isOk());

        verify(jobService).updateJob(any(), any(Job.class));
        verify(jobService, never()).createJob(any(Job.class));
    }

    @Test
    void deleteReturnsNoContentWhenAllowed() throws Exception {
        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteWithLinkedApplicationsReturnsConflict() throws Exception {
        org.mockito.Mockito.doThrow(new JobDeletionBlockedException("This job cannot be deleted because applications are linked to it"))
                .when(jobService).deleteJob(1L);

        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void statisticsRequiresAdminAndReturnsJson() throws Exception {
        when(dashboardService.getDashboardStatistics()).thenReturn(new DashboardStatisticsResponse(1, 1, 0, 2, 3, 1, 1, 0));

        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(1));
    }

    private String validJobJson() {
        return """
                {
                  "title": "Developer",
                  "companyName": "Company",
                  "location": "Toronto",
                  "category": "Technology",
                  "employmentType": "CO_OP",
                  "description": "Build apps",
                  "requirements": "Java",
                  "status": "ACTIVE"
                }
                """;
    }

    private Job job(Long id, String title, String status) {
        Job job = new Job();
        job.setJobId(id);
        job.setTitle(title);
        job.setCompanyName("Company");
        job.setLocation("Toronto");
        job.setCategory("Technology");
        job.setEmploymentType("CO_OP");
        job.setDescription("Build apps");
        job.setRequirements("Java");
        job.setStatus(status);
        job.setPostedDate(LocalDate.now());
        return job;
    }
}
