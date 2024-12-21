package dev.ramil21.lab4back.repository;

import dev.ramil21.lab4back.model.Point;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Point p WHERE p.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

}
