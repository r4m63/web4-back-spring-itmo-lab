package dev.ramil21.lab4back.controller;

import dev.ramil21.lab4back.dto.*;
import dev.ramil21.lab4back.service.AuthService;
import dev.ramil21.lab4back.util.MailUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/signup/verification")
    public ResponseEntity<AccessToken> doSignupVerification(@RequestBody VerificationToken request, HttpServletResponse response) {
        AccessToken res = authService.verification(request.getToken(), response);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/signin")
    public ResponseEntity<AccessToken> doSignin(@RequestBody UserCredentialsRequest request, HttpServletResponse response) {
        AccessToken res = authService.signin(request.getEmail(), request.getPassword(), response);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("refresh-tokens")
    public ResponseEntity<AccessToken> doRefreshTokens(@RequestBody RefreshTokenRequest request, HttpServletResponse response) {
        AccessToken res = authService.refreshTokens(request.getRefreshToken(), response);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    // TODO: посмотреть что с request.getCredentials(), что можно полезного брать от туда
    @PostMapping("/google-login")
    public ResponseEntity<AccessToken> doGoogleLogin(@RequestBody GoogleLoginRequest request, HttpServletResponse response) throws Exception {
        AccessToken res = authService.googleLogin(request.getToken(), response);
        return ResponseEntity.status(HttpStatus.OK).body(res);
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
