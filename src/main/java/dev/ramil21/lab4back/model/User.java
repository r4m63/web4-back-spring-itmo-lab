package dev.ramil21.lab4back.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    //TODO: @lobmok.Data or @override hashCode() equals() toString()

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "verification_token", unique = true)
    private String verificationToken;

    @Column(name = "is_google_auth")
    private Boolean isGoogleAuth;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.getDefaultRole();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<RefreshToken> refreshTokens;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
