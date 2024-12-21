package dev.ramil21.lab4back.util.mail;

/**
 * Usage example:
 * String username = "rm.tj.777@gmail.com";
 * String htmlBody = EmailTemplates.MAIL_WELCOME.format(username);
 */
public enum MailTemplates {

    // SUBJECTS TEMPLATES
    SUBJECT_LOGIN("Вход"),
    SUBJECT_RESET_PASSWORD("Восстановление пароля"),
    SUBJECT_REGISTRATION("Регистрация нового пользователя"),

    // BODY TEMPLATES
    BODY_LOGIN("<p>Только что произошел вход в аккаунт %s!</p>"),
    BODY_RESET_PASSWORD("<p>Hi, %s!</p><p>Click <a href='%s'>here</a> to reset your password.</p>"),
    BODY_REGISTRATION("<p>Перейдите по ссылке чтобы подтвердить аккаунт: %s</p>");

    private final String template;

    MailTemplates(String template) {
        this.template = template;
    }

    public String set(Object... args) {
        return String.format(template, args);
    }

    public String get() {
        return template;
    }
}
