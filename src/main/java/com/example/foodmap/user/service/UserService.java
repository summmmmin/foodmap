package com.example.foodmap.user.service;

import com.example.foodmap.common.error.*;
import com.example.foodmap.user.domain.User;
import com.example.foodmap.user.domain.UserRepository;
import com.example.foodmap.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) { this.repo = repo; }

    public UserResponse getById(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(ErrorCode.USER_NOT_FOUND, "사용자 없음: " + id));
        return UserResponse.from(user);
    }
}
