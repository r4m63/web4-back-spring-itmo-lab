package dev.ramil21.lab4back.util.mail;

/**
 * Usage example:
 * String username = "rm.tj.777@gmail.com";
 * String htmlBody = EmailTemplates.MAIL_WELCOME.format(username);
 */
public enum MailTemplates {

    // SUBJECTS TEMPLATES
    SUBJECT_LOGIN(""),
    SUBJECT_RESET_PASSWORD(""),
    SUBJECT_REGISTRATION("Регистрация нового пользователя"),

    // BODY TEMPLATES
    BODY_LOGIN("<h1>Welcome, %s!</h1><p>We're glad you're here.</p>"),
    BODY_RESET_PASSWORD("<p>Hi, %s!</p><p>Click <a href='%s'>here</a> to reset your password.</p>"),
    BODY_REGISTRATION("<h1>xD</h1><br><p>Перейдите по ссылке чтобы подтвердить аккаунт: %s</p>");

    private final String template;

    MailTemplates(String template) {
        this.template = template;
    }

    // Метод для шаблонов, которые принимают аргументы
    public String set(Object... args) {
        return String.format(template, args);
    }

    // Метод для шаблонов, которые не принимают аргументы
    public String get() {
        return template;
    }
}
