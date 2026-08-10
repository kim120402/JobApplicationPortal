package com.example.Job_Application_Portal.controller;

import com.example.Job_Application_Portal.model.User;
import com.example.Job_Application_Portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthControllerTest {

    @Test
    void postLogoutInvalidatesSession() {
        AuthController controller = new AuthController(mock(UserService.class));
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setUserId(1L);
        user.setRole("APPLICANT");
        session.setAttribute("loggedInUser", user);

        String view = controller.logout(session);

        assertThat(view).isEqualTo("redirect:/login?logout");
        assertThat(session.isInvalid()).isTrue();
    }
}
