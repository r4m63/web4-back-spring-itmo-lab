package dev.ramil21.lab4back.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OAuthUtil {

    public String addUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            if (oAuth2User != null) {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                return "Hello, " + attributes.get("given_name") + " | " + attributes.get("family_name") + " | " + attributes.get("email") + " | " + attributes.get("username") + " | " + attributes.get("picture") + " | ";
            }
        }
        return "No OAuth2AuthenticationToken found";
    }

}
