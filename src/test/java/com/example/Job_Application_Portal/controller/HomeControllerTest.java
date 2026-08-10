package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class HomeControllerTest {
    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new HomeController(userService)).build();
    }

    @Test
    void publicUserClicksLogoAndOpensHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeDoesNotExist("loggedInUser"));
    }

    @Test
    void applicantClicksLogoAndRemainsApplicant() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User sessionUser = user(1L, "APPLICANT");
        User databaseUser = user(1L, "APPLICANT");
        session.setAttribute("loggedInUser", sessionUser);
        when(userService.getUserById(1L)).thenReturn(databaseUser);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("loggedInUser", databaseUser));

        assertThat(session.getAttribute("loggedInUser")).isSameAs(databaseUser);
        assertThat(((User) session.getAttribute("loggedInUser")).getRole()).isEqualTo("APPLICANT");
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void adminClicksLogoAndRemainsAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User sessionUser = user(2L, "ADMIN");
        User databaseUser = user(2L, "ADMIN");
        session.setAttribute("loggedInUser", sessionUser);
        when(userService.getUserById(2L)).thenReturn(databaseUser);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("loggedInUser", databaseUser));

        assertThat(session.getAttribute("loggedInUser")).isSameAs(databaseUser);
        assertThat(((User) session.getAttribute("loggedInUser")).getRole()).isEqualTo("ADMIN");
        assertThat(session.isInvalid()).isFalse();
        verify(userService).getUserById(2L);
    }

    @Test
    void getHomeDoesNotReplaceAdminWithNewApplicant() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User admin = user(3L, "ADMIN");
        session.setAttribute("loggedInUser", admin);
        when(userService.getUserById(3L)).thenReturn(admin);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk());

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        assertThat(loggedInUser.getUserId()).isEqualTo(3L);
        assertThat(loggedInUser.getRole()).isEqualTo("ADMIN");
        assertThat(session.isInvalid()).isFalse();
    }

    private User user(Long userId, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName(role + " User");
        user.setRole(role);
        return user;
    }
}
