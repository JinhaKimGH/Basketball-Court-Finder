package com.basketballcourtfinder.service;

import com.basketballcourtfinder.dto.UserProjection;
import com.basketballcourtfinder.entity.User;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.repository.UserRepository;
import com.basketballcourtfinder.util.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RestClientTest(UserService.class)
@AutoConfigureDataJpa
public class UserServiceTest {
    @Autowired
    private UserService service;

    @MockitoBean
    private UserRepository repository;

    @MockitoBean
    private PasswordUtils passwordUtils;

    @Test
    public void test_get() {
        long mock_userId = 1L;
        UserProjection mockUser = new UserProjection() {
            @Override
            public String getEmail() {
                return "test@example.com";
            }

            @Override
            public String getDisplayName() {
                return "Test User";
            }
        };
        when(repository.findProjectedById(mock_userId)).thenReturn(Optional.of(mockUser));

        UserProjection user = service.get(mock_userId);

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getDisplayName());
    }

    @Test
    public void test_get_user_not_found() {
        long mock_userId = 1L;

        when(repository.findById(mock_userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.get(mock_userId);
        });

        assertEquals(exception.getMessage(), "user with ID " + mock_userId + " not found");
    }

    @Test
    public void test_login_success() throws NoSuchAlgorithmException {
        String email = "test@test.com";
        String password = "password";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setPassword(password);
        mockUser.setSalt("salt");

        when(repository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordUtils.hashPassword(password, "salt")).thenReturn(password);
        when(passwordUtils.generateToken(mockUser)).thenReturn("token");

        String token = service.login(email, password);

        assertNotNull(token);
        assertEquals("token", token);
    }

    @Test
    public void test_login_user_not_found() throws NoSuchAlgorithmException {
        String email = "test@test.com";
        String password = "password";

        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        assertNull(service.login(email, password));
    }

    @Test
    public void test_login_invalid_password() throws NoSuchAlgorithmException {
        String email = "test@test.com";
        String password = "password";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setPassword("wrong_password");
        mockUser.setSalt("salt");

        when(repository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        assertNull(service.login(email, password));
    }

    @Test
    public void test_save_user_success() throws NoSuchAlgorithmException {
        String email = "test@test.com";
        String password = "password";
        String displayName = "Test User";

        when(repository.findByEmail(email)).thenReturn(Optional.empty());
        when(repository.findByDisplayName(displayName)).thenReturn(Optional.empty());

        service.saveUser(email, password, displayName);

        verify(repository).save(any(User.class));
    }

    @Test
    public void test_save_user_email_already_exists() throws NoSuchAlgorithmException {
        String email = "test@test.com";
        String password = "password";
        String displayName = "Test User";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);

        when(repository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        EntityAlreadyExistsException exception = assertThrows(EntityAlreadyExistsException.class, () -> {
            service.saveUser(email, password, displayName);
        });

        assertEquals(exception.getMessage(), "Email is already registered");
    }

    @Test
    public void test_save_user_display_name_already_exists() throws NoSuchAlgorithmException {
        String email = "test@test.com";
        String password = "password";
        String displayName = "Test User";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setDisplayName(displayName);

        when(repository.findByDisplayName(displayName)).thenReturn(Optional.of(mockUser));

        EntityAlreadyExistsException exception = assertThrows(EntityAlreadyExistsException.class, () -> {
            service.saveUser(email, password, displayName);
        });

        assertEquals(exception.getMessage(), "Display name is already taken");
    }

    @Test
    public void test_delete_user_success() {
        long mock_userId = 1L;

        User mockUser = new User();
        mockUser.setId(mock_userId);

        when(repository.findById(mock_userId)).thenReturn(Optional.of(mockUser));

        boolean result = service.deleteUser(mock_userId);

        assertTrue(result);
        verify(repository).deleteById(mock_userId);
    }

    @Test
    public void test_delete_user_not_found() {
        long mock_userId = 1L;

        when(repository.findById(mock_userId)).thenReturn(Optional.empty());

        boolean result = service.deleteUser(mock_userId);

        assertFalse(result);
    }

    @Test
    public void test_update_email_success() {
        long mock_userId = 1L;
        String newEmail = "new@test.com";

        User mockUser = new User();
        mockUser.setId(mock_userId);

        when(repository.findById(mock_userId)).thenReturn(Optional.of(mockUser));

        service.updateEmail(mock_userId, newEmail);

        verify(repository).save(any(User.class));
    }

    @Test
    public void test_update_email_invalid_email() {
        long mock_userId = 1L;
        String newEmail = "test@test.com";

        User mockUser = new User();
        mockUser.setId(mock_userId);
        mockUser.setEmail("test@test.com");

        when(repository.findById(mock_userId)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.updateEmail(mock_userId, newEmail);
        });

        assertEquals(exception.getMessage(), "The new email address must be different from the current email address.");
    }

    @Test
    public void test_update_password_success() throws NoSuchAlgorithmException {
        long mock_userId = 1L;
        String newPassword = "new_password";

        User mockUser = new User();
        mockUser.setId(mock_userId);
        mockUser.setPassword("old_password");
        mockUser.setSalt("salt");

        when(repository.findById(mock_userId)).thenReturn(Optional.of(mockUser));
        when(passwordUtils.hashPassword("new_password", "salt")).thenReturn("new_password");
        service.updatePassword(mock_userId, newPassword);

        verify(repository).save(any(User.class));
    }

    @Test
    public void test_update_password_invalid_password() throws NoSuchAlgorithmException {
        long mock_userId = 1L;
        String newPassword = "old_password";

        User mockUser = new User();
        mockUser.setId(mock_userId);
        mockUser.setPassword("old_password");
        mockUser.setSalt("salt");

        when(repository.findById(mock_userId)).thenReturn(Optional.of(mockUser));
        when(passwordUtils.hashPassword("old_password", "salt")).thenReturn("old_password");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.updatePassword(mock_userId, newPassword);
        });

        assertEquals(exception.getMessage(), "The new password must be different from the current password.");
    }

    @Test
    public void test_update_display_name_success() {
        long mock_userId = 1L;
        String newDisplayName = "New User";

        User mockUser = new User();
        mockUser.setId(mock_userId);
        mockUser.setDisplayName("Old User");

        when(repository.findById(mock_userId)).thenReturn(Optional.of(mockUser));

        service.updateDisplayName(mock_userId, newDisplayName);

        verify(repository).save(any(User.class));
    }

    @Test
    public void test_update_display_name_invalid_displayName() {
        long mock_userId = 1L;
        String newDisplayName = "Old User";

        User mockUser = new User();
        mockUser.setId(mock_userId);
        mockUser.setDisplayName("Old User");

        when(repository.findById(mock_userId)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.updateDisplayName(mock_userId, newDisplayName);
        });

        assertEquals(exception.getMessage(), "The new display name must be different from the current display name.");
    }
}
