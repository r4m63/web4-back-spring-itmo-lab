package dev.ramil21.lab4back.repository.token;

import dev.ramil21.lab4back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenRepository, Integer> {
}
