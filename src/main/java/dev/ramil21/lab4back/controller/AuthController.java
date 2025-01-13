package dev.ramil21.lab4back.controller;

import dev.ramil21.lab4back.dto.*;
import dev.ramil21.lab4back.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> doSignup(@RequestBody UserCredentialsRequest request) {
        authService.signup(request.getEmail(), request.getPassword());
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

    // TODO: посмотреть что с request.getCredentials(), что можно полезного брать от туда
    @PostMapping("/google-login")
    public ResponseEntity<AccessToken> doGoogleLogin(@RequestBody GoogleLoginRequest request, HttpServletResponse response) throws Exception {
        AccessToken res = authService.googleLogin(request.getToken(), response);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("refresh-tokens")
    public ResponseEntity<AccessToken> doRefreshTokens(@RequestBody RefreshTokenRequest request, HttpServletResponse response) {
        AccessToken res = authService.refreshTokens(request.getRefreshToken(), response);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/passreset")
    public ResponseEntity<Void> doResetPassword(@RequestBody UserCredentialsRequest request) throws Exception {
        authService.passReset(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // TODO: METHOD: CHECK token in REACT useEffect after get into 'http://localhost:5173/signin/reset-password'
    @PostMapping("/passreset/verify")
    public ResponseEntity<Void> doResetPasswordVerify(@RequestBody PasResetTokenDTO req) throws Exception {
        if (authService.passResetVerify(req.getToken()))
            return ResponseEntity.status(HttpStatus.OK).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/passreset/confirm")
    public ResponseEntity<Void> doResetPasswordConfirm(@RequestBody PasswordResetDTO request) throws Exception {
        authService.passResetConfirm(request.getPassword(), request.getToken());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
