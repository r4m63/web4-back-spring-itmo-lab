package dev.ramil21.lab4back.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordUtil {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hashPassword(String password) {
        return encoder.encode(password);
    }

    public boolean verifyPassword(String userPass, String dbPass) {
        return encoder.matches(userPass, dbPass);
    }
}
