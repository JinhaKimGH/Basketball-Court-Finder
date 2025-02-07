package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.LoginDTO;
import com.basketballcourtfinder.dto.UserDTO;
import com.basketballcourtfinder.dto.UserProjection;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    UserService service;

    @Mock
    Authentication authentication;

    public void setup_authUser() {
        // Create a mocked Authentication object
        Long mockUserId = 1L;
        authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUserId);

        // Mock the SecurityContextHolder
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetUser_Success() throws Exception {
        setup_authUser();

        Long mockUserId = 1L;

        // Mock service response
        UserProjection mockUser = new UserProjection() {
            @Override
            public String getEmail() {
                return "testuser@example.com";
            }

            @Override
            public String getDisplayName() {
                return "Test User";
            }
        };

        when(service.get(mockUserId)).thenReturn(mockUser);

        // Perform the request
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.displayName").value("Test User"));
    }

    @Test
    public void testGetUser_Error() throws Exception {
        setup_authUser();

        Long mockUserId = 1L;

        // User not found in service
        when(service.get(mockUserId)).thenThrow(new EntityNotFoundException("user", mockUserId));

        // Perform request
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("user with ID 1 not found"));
    }

    @Test
    public void testSignUpUser_EmailValidationError() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("invalid_mail");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format"));
    }

    @Test
    public void testSignUpUser_Fail() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@gmail.com");
        userDTO.setPassword("password");
        userDTO.setDisplayName("a");

        // Convert UserDTO to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Mock service
        doThrow(new EntityAlreadyExistsException("User already exists"))
                .when(service).saveUser(userDTO.getEmail(), userDTO.getPassword(), userDTO.getDisplayName());

        // Perform request
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already exists"));
    }

    @Test
    public void testSignUpUser_Success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@gmail.com");
        userDTO.setPassword("password");
        userDTO.setDisplayName("a");

        // Convert UserDTO to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Mock service
        doNothing().when(service).saveUser(userDTO.getEmail(), userDTO.getPassword(), userDTO.getDisplayName());

        // Perform request
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("User added successfully."));
    }

    @Test
    public void testLogin_Fail() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("a@email.com");
        loginDTO.setPassword("password");

        // Convert loginDTO to json
        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(loginDTO);

        // Mock service
        when(service.login(loginDTO.getEmail(), loginDTO.getPassword())).thenReturn(null);

        // Perform request
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid Credentials!"));
    }

    @Test
    public void testLogin_Success() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("a@email.com");
        loginDTO.setPassword("password");

        // Convert loginDTO to json
        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(loginDTO);

        // Mock service
        when(service.login(loginDTO.getEmail(), loginDTO.getPassword())).thenReturn("token");

        // Perform request
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(content().string("token"));
    }

    @Test
    public void testUpdateUser_EmailValidationError() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("invalid_mail");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(put("/api/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format"));
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@email.com");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        Long mockUserId = 1L;

        // Mock Service
        doNothing().when(service).updateEmail(mockUserId, userDTO.getEmail());

        // Perform request
        mockMvc.perform(put("/api/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }

    @Test
    public void testUpdateUser_badRequest() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        UserDTO userDTO = new UserDTO();

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(put("/api/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("At least one field must not be null."));
    }

    @Test
    public void testUpdateUser_notFound() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@email.com");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        Long mockUserId = 1L;

        // Mock Service
        doThrow(new EntityNotFoundException("user", mockUserId)).when(service).updateEmail(mockUserId, userDTO.getEmail());

        // Perform request
        mockMvc.perform(put("/api/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("user with ID 1 not found"));
    }

    @Test
    public void testDeleteUser_Success() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        long mockUserId = 1L;

        // Mock Service
        when(service.deleteUser(mockUserId)).thenReturn(true);

        // Perform request
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    public void testDeleteUser_Fail() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        long mockUserId = 1L;

        // Mock Service
        when(service.deleteUser(mockUserId)).thenReturn(false);

        // Perform request
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }
}
