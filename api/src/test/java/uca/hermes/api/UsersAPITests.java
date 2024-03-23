package uca.hermes.api;

import uca.hermes.api.controller.UserController;
import uca.hermes.api.dao.User;
import uca.hermes.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class UsersAPITests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private User user_Main;
    private List<User> usersList_GET;

    private final String API_URL = "/basic-auth/api/users";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    public void init() {
        user_Main = new User("test", "password", false, null, null);
        usersList_GET = new ArrayList<User>();
        usersList_GET.add(new User("test1", "password1", false, null, null));
        usersList_GET.add(new User("test2", "password2", false, null, null));
        usersList_GET.add(new User("test3", "password3", false, null, null));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void TestGetAllUsers() throws Exception {
        when(service.findAll()).thenReturn(usersList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(3)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void TestPostUser() throws Exception {
        User user = new User("test4", "password4", false, null, null);
        doNothing().when(service).save(any(User.class));

        ResultActions resp = mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        resp.andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", CoreMatchers.is("test4")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.password", CoreMatchers.is("password4")));
    }

}