package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.LoginDTO;
import com.basketballcourtfinder.dto.UserDTO;
import com.basketballcourtfinder.dto.UserProjection;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
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

import java.util.HashMap;
import java.util.Map;

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
        userDTO.setDisplayName("invalid");
        userDTO.setPassword("Abcdefghi123$");
        userDTO.setReenterPassword("Abcdefghi123$");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format."));
    }

    @Test
    public void testSignUpUser_PasswordValidationError() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@gmail.com");
        userDTO.setPassword("Password3%$");
        userDTO.setReenterPassword("Password2%$");
        userDTO.setDisplayName("abca");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Passwords are not the same."));
    }

    @Test
    public void testSignUpUser_DisplayNameSizeValidationError() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@gmail.com");
        userDTO.setPassword("Password2%$");
        userDTO.setReenterPassword("Password2%$");
        userDTO.setDisplayName("a");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Display name must be between 3 and 20 characters."));
    }

    @Test
    public void testSignUpUser_DisplayNameStringValidationError() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@gmail.com");
        userDTO.setPassword("Password2%$");
        userDTO.setReenterPassword("Password2%$");
        userDTO.setDisplayName("a$%$^&$@#$");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Display name can only contain letters, numbers, and spaces."));
    }

    @Test
    public void testSignUpUser_Fail() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("a@gmail.com");
        userDTO.setPassword("Password2$");
        userDTO.setReenterPassword("Password2$");
        userDTO.setDisplayName("abca");

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
        userDTO.setPassword("Password2$");
        userDTO.setReenterPassword("Password2$");
        userDTO.setDisplayName("abc");

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
        Map<String, String> map = new HashMap<>();
        map.put("token", "token");
        map.put("displayName", "a");
        map.put("email", "a@email.com");
        when(service.login(loginDTO.getEmail(), loginDTO.getPassword())).thenReturn(map);

        // Perform request
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("a"))
                .andExpect(jsonPath("$.email").value("a@email.com"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    public void testUpdateUser_EmailValidationError() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("invalid_mail");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        // Perform request
        mockMvc.perform(put("/api/users/email")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format."));
    }

    @Test
    public void testUpdateUserEmail_Success() throws Exception {
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
        mockMvc.perform(put("/api/users/email")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User email updated successfully"));
    }

    @Test
    public void testUpdateUserDisplayName_Success() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        UserDTO userDTO = new UserDTO();
        userDTO.setDisplayName("abc1234");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        Long mockUserId = 1L;

        // Mock Service
        doNothing().when(service).updateEmail(mockUserId, userDTO.getEmail());

        // Perform request
        mockMvc.perform(put("/api/users/displayName")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User display name updated successfully"));
    }

    @Test
    public void testUpdateUserPassword_Success() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("Abc1234$");

        // Convert the User object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDTO);

        Long mockUserId = 1L;

        // Mock Service
        doNothing().when(service).updateEmail(mockUserId, userDTO.getEmail());

        // Perform request
        mockMvc.perform(put("/api/users/password")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User password updated successfully"));
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
        mockMvc.perform(put("/api/users/email")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is required."));
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
        mockMvc.perform(put("/api/users/email")
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

    @Test
    public void testGetUserStats_Fail() throws Exception {
        mockMvc.perform(get("/api/users/stats"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User is not authenticated"));
    }

    @Test
    public void testGetUserStats_NotFound() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        long mockUserId = 1L;

        when(service.findUserStats(mockUserId)).thenThrow(new EntityNotFoundException("user", mockUserId));
        mockMvc.perform(get("/api/users/stats"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("user with ID 1 not found"));
    }

    @Test
    public void testGetUserStats_Success() throws Exception {
        // Sets up authentication and security context holder
        setup_authUser();

        long mockUserId = 1L;
        Map<String, String> map = new HashMap<>();
        map.put("trust", "1.0");
        map.put("review_count", "10");

        when(service.findUserStats(mockUserId)).thenReturn(map);
        mockMvc.perform(get("/api/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trust").value("1.0"))
                .andExpect(jsonPath("$.review_count").value("10"));
    }
}
