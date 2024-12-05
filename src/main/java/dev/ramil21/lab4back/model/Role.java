package dev.ramil21.lab4back.model;

public enum Role {
    USER,
    ADMIN;

    public static Role getDefaultRole() {
        return USER;
    }
}
