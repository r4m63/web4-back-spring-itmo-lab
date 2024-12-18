package dev.ramil21.lab4back.repository;

import dev.ramil21.lab4back.model.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Integer> {

    List<Point> findByUserId(Long userId);

}
