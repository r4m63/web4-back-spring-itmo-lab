package dev.ramil21.lab4back.dto;

import dev.ramil21.lab4back.model.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointDTO {
    private Long id;
    private float x;
    private float y;
    private float r;
    private boolean hit;
    private LocalDateTime createdAt;

    public PointDTO(Point point) {
        this.id = point.getId();
        this.x = point.getX();
        this.y = point.getY();
        this.r = point.getR();
        this.hit = point.isResult();
        this.createdAt = point.getCreatedAt();
    }
}
