package dev.ramil21.lab4back.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
class SimpleAuth extends AbstractAuthenticationToken {

    private final String email;

    public SimpleAuth(String email) {
        super(null);  // Нет authorities, так как только email передается
        this.email = email;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

}
