package dev.ramil21.lab4back.controller;

import dev.ramil21.lab4back.dto.RefreshTokenRequest;
import dev.ramil21.lab4back.dto.UserCredentialsRequest;
import dev.ramil21.lab4back.dto.TokensResponse;
import dev.ramil21.lab4back.service.AuthService;
import dev.ramil21.lab4back.util.MailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private AuthService authService;
    private MailUtil mailSenderService;

    @Autowired
    public AuthController(AuthService authService, MailUtil mailSenderService) {
        this.authService = authService;
        this.mailSenderService = mailSenderService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> doSignup(@RequestBody UserCredentialsRequest request) {
        authService.signup(request.getEmail(), request.getPassword());
        // TODO: try catch обработка ошибок по HttpStatus'ам
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/signup/verification") // TODO: remake to POST
    public ResponseEntity<TokensResponse> doSignupVerification(@RequestParam("token") String token) {
        TokensResponse response = authService.verification(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login") // TODO: remake to POST
    public ResponseEntity<TokensResponse> doLogin(@RequestParam("email") String email, @RequestParam("password") String password) {
        TokensResponse response = authService.login(email, password);
        return ResponseEntity.ok(response);
    }

    @PostMapping("refresh-tokens")
    public ResponseEntity<TokensResponse> doRefreshTokens(@RequestBody RefreshTokenRequest request) {
        TokensResponse response = authService.refreshTokens(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }



    @GetMapping("/mail")
    public ResponseEntity<String> sayHelloFromCat() {
        try {
            mailSenderService.send(
                    "rm.tj.777@gmail.com",
                    "Hello From Ramil",
                    "Hello, my name is Ramil. Have a nice day!"
            );
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

}
