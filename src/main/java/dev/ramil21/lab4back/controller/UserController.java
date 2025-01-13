package dev.ramil21.lab4back.controller;

import dev.ramil21.lab4back.dto.PasswordResetDTO;
import dev.ramil21.lab4back.dto.PointDTO;
import dev.ramil21.lab4back.dto.PointHitResponse;
import dev.ramil21.lab4back.dto.UserPointRequest;
import dev.ramil21.lab4back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
//@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/check-point")
    public ResponseEntity<PointHitResponse> checkPoint(@RequestBody UserPointRequest request) {
        PointHitResponse res = userService.checkPointHit(request.getX(), request.getY(), request.getR(), request.getToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/all-points")
    public ResponseEntity<List<PointDTO>> allPoints(@RequestBody UserPointRequest request) {
        List<PointDTO> res = userService.getAllPoints(request.getToken());
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/delete-points")
    public ResponseEntity<Void> deleteAllPoints(@RequestBody UserPointRequest request) {
        userService.clearAllPoints(request.getToken());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/user/change-pass")
    public ResponseEntity<Void> changePass(@RequestBody PasswordResetDTO req) {
        userService.changePassword(req.getPassword(), req.getToken());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
