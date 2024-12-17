package dev.ramil21.lab4back.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "points")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "x")
    private float x;

    @Column(name = "y")
    private float y;

    @Column(name = "r")
    private float r;

    @Column(name = "res")
    private boolean result;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
