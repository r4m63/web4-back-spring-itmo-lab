package dev.ramil21.lab4back.repository;

import dev.ramil21.lab4back.model.PassResetToken;
import dev.ramil21.lab4back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PassResetTokenRepository extends JpaRepository<PassResetToken, Long> {
    Optional<PassResetToken> findByToken(String token);

    boolean existsByToken(String token);

    @Query("SELECT prt.user FROM PassResetToken prt WHERE prt.token = :token")
    Optional<User> findUserByToken(@Param("token") String token);
}
