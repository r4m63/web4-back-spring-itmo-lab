package dev.ramil21.lab4back.controller;

import com.nimbusds.oauth2.sdk.TokenResponse;
import dev.ramil21.lab4back.dto.SignupResponse;
import dev.ramil21.lab4back.dto.SignupRequest;
import dev.ramil21.lab4back.dto.SignupVerificationRequest;
import dev.ramil21.lab4back.dto.SignupVerificationResponse;
import dev.ramil21.lab4back.service.AuthService;
import dev.ramil21.lab4back.util.MailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;
    @Autowired
    private final MailUtil mailSenderService;

    @PostMapping("/signup")
    public ResponseEntity<Void> doSignup(@RequestBody SignupRequest request) {
        authService.signup(request.getEmail(), request.getPassword());
        // TODO: try catch обработка ошибок по HttpStatus'ам
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/signup/verification")
    public ResponseEntity<SignupVerificationResponse> doSignupVerification(@RequestParam("token") String token) {
        SignupVerificationResponse response = authService.verification(token);
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
