package dev.ramil21.lab4back.repository;

import dev.ramil21.lab4back.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
