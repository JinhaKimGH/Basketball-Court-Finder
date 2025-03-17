package com.basketballcourtfinder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDTO {
    @NotNull(message="Email cannot be null")
    @Size(max = 100, message = "Email cannot be longer than 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;
}
