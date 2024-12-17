package dev.ramil21.lab4back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointHitResponse {
    private float x;
    private float y;
    private float r;
    private boolean hit;

}
