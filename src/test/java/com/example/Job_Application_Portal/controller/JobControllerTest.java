package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobControllerTest {
    private JobService jobService;
    private JobController jobController;

    @BeforeEach
    void setUp() {
        jobService = mock(JobService.class);
        jobController = new JobController(jobService);
    }

    @Test
    void browseJobsPreservesApplicantSession() throws Exception {
        MockHttpSession session = sessionUser("APPLICANT");
        ConcurrentModel model = new ConcurrentModel();
        when(jobService.getActiveJobs()).thenReturn(List.of());

        String view = jobController.showJobs(session, model);

        assertThat(view).isEqualTo("jobs");
        assertThat(model.getAttribute("loggedInUser")).isSameAs(session.getAttribute("loggedInUser"));
        assertThat(session.getAttribute("loggedInUser")).isNotNull();
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void searchJobsPreservesApplicantSession() throws Exception {
        MockHttpSession session = sessionUser("APPLICANT");
        ConcurrentModel model = new ConcurrentModel();
        when(jobService.getActiveJobs()).thenReturn(List.of());

        String view = jobController.searchJobs("developer", null, null, null, session, model);

        assertThat(view).isEqualTo("jobs");
        assertThat(model.getAttribute("loggedInUser")).isSameAs(session.getAttribute("loggedInUser"));
        assertThat(session.getAttribute("loggedInUser")).isNotNull();
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void browseJobsPreservesAdminSession() throws Exception {
        MockHttpSession session = sessionUser("ADMIN");
        ConcurrentModel model = new ConcurrentModel();
        when(jobService.getActiveJobs()).thenReturn(List.of());

        String view = jobController.showJobs(session, model);

        assertThat(view).isEqualTo("jobs");
        assertThat(model.getAttribute("loggedInUser")).isSameAs(session.getAttribute("loggedInUser"));
        assertThat(session.getAttribute("loggedInUser")).isNotNull();
        assertThat(session.isInvalid()).isFalse();
    }

    private MockHttpSession sessionUser(String role) {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setUserId(1L);
        user.setRole(role);
        session.setAttribute("loggedInUser", user);
        return session;
    }
}
