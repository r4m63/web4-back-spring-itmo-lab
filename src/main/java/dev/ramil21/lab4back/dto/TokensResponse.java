package dev.ramil21.lab4back.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokensResponse {
    //@JsonProperty("access_token")
    private String accessToken;
    private String refreshToken;
}
