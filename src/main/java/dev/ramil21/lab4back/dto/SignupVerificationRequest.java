package dev.ramil21.lab4back.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupVerificationRequest {
    private String token;
}
