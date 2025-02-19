package com.basketballcourtfinder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String password;

    @NotBlank(message = "Please re-enter your password.")
    private String reenterPassword;

    @NotBlank(message = "Display name is required.")
    @Size(min = 3, max = 20, message = "Display name must be between 3 and 20 characters.")
    @Pattern(
            regexp = "^[A-Za-z0-9 ]+$",
            message = "Display name can only contain letters, numbers, and spaces."
    )
    private String displayName;
}
