package com.coffee.controller;

import com.coffee.common.Result;
import com.coffee.dto.LoginDTO;
import com.coffee.dto.RegisterDTO;
import com.coffee.entity.User;
import com.coffee.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        try {
            return Result.success(userService.login(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            return Result.success(userService.register(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }
        User user = userService.getUserByToken(token.substring(7));
        if (user == null) {
            return Result.error(401, "登录已过期");
        }
        return Result.success(user);
    }

    @GetMapping("/baristas")
    public Result<List<User>> getBaristas() {
        return Result.success(userService.getUsersByRole("BARISTA"));
    }
}
