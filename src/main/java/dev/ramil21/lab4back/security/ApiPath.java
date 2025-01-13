package dev.ramil21.lab4back.security;

/**
 * '/' пишется всегда только вначале пути
 */
public enum ApiPath {

    FRONTEND_BASE_URL_PROD("http://ramil21.ru"),
    FRONTEND_BASE_URL_DEV("http://localhost:5173"),

    BACKEND_BASE_URL_PROD("https://localhost:8080"),
    BACKEND_BASE_URL_DEV("https://localhost:8080"),

    URL_API_AUTH("/api/auth"),

    URL_AUTH_SIGNUP("/signup"),
    URL_AUTH_SIGNUP_VERIFICATION("/signup/verification"),
    URL_AUTH_SIGNIN("/signin"),
    URL_AUTH_REFRESH_TOKENS("/refresh-tokens"),
    URL_AUTH_GOOGLE_LOGIN("/google-login"),

    URL_API_USER("/api/auth"),

    URL_USER_CHECK_POINT("/check-point");

    private final String template;

    ApiPath(String template) {
        this.template = template;
    }

    public String set(Object... args) {
        return String.format(template, args);
    }

    public String get() {
        return template;
    }

}
