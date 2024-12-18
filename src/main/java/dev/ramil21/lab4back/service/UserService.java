package dev.ramil21.lab4back.service;

import dev.ramil21.lab4back.dto.PointHitResponse;
import dev.ramil21.lab4back.model.Point;
import dev.ramil21.lab4back.model.User;
import dev.ramil21.lab4back.repository.PointRepository;
import dev.ramil21.lab4back.repository.UserRepository;
import dev.ramil21.lab4back.util.PointCheckerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    UserRepository userRepository;
    PointRepository pointRepository;

    @Autowired
    public UserService(UserRepository userRepository, PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.pointRepository = pointRepository;
    }

    public PointHitResponse checkPointHit(float x, float y, float r) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (email != null) {
            System.out.println("Email from JWT: " + email);
        } else {
            System.out.println("No email found in JWT token.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User not found"));

        boolean isHit = PointCheckerUtil.checkHit(x, y, r);
        Point point = Point.builder()
                .x(x)
                .y(y)
                .r(r)
                .result(isHit)
                .user(user)
                .build();
        pointRepository.save(point);

        return new PointHitResponse(x, y, r, isHit);
    }

    public List<Point> getAllPoints() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (email != null) {
            System.out.println("Email from JWT: " + email);
        } else {
            System.out.println("No email found in JWT token.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User not found"));

        return pointRepository.findByUserId(user.getId());
    }

}
