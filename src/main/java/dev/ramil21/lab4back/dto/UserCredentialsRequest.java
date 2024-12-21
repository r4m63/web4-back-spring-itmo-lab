package dev.ramil21.lab4back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Jacksonized
public class UserCredentialsRequest {
    private String username;
    private String email;
    private String password;
}

