package dev.ramil21.lab4back.controller;

import dev.ramil21.lab4back.dto.PointHitResponse;
import dev.ramil21.lab4back.dto.UserPointRequest;
import dev.ramil21.lab4back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/check-point")
    public ResponseEntity<PointHitResponse> checkPoint(@RequestBody UserPointRequest request) {
        PointHitResponse res = userService.checkPointHit(request.getX(), request.getY(), request.getR());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

//    @PostMapping("/all-points")
//    public ResponseEntity<Void> allPoints() {
//
//    }

}
