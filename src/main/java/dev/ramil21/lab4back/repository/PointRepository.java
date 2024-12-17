package dev.ramil21.lab4back.repository;

import dev.ramil21.lab4back.model.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Integer> {
}
