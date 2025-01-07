package com.basketballcourtfinder.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserDTO {
    @Email(message = "Invalid email format")
    private String email;
    private String password;
    private String displayName;
}
