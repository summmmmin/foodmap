package com.example.foodmap.user.controller;

import com.example.foodmap.user.dto.UserResponse;
import com.example.foodmap.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @Operation(summary = "사용자 단건 조회", description = "ID로 사용자 조회")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
