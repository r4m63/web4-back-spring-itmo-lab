package dev.ramil21.lab4back.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupVerificationResponse {
    private String accessToken;
    private String refreshToken;
}
