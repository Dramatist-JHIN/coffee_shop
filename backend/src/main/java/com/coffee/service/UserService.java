package com.coffee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.dto.LoginDTO;
import com.coffee.dto.RegisterDTO;
import com.coffee.entity.User;
import com.coffee.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        // 简化：直接用用户ID作为token
        result.put("token", String.valueOf(user.getId()));
        result.put("user", sanitizeUser(user));
        return result;
    }

    public User register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(dto.getRole());
        user.setAvatar("/avatars/default.png");
        userMapper.insert(user);

        return sanitizeUser(user);
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        return user != null ? sanitizeUser(user) : null;
    }

    // 简化：直接解析用户ID
    public User getUserByToken(String token) {
        try {
            Long userId = Long.parseLong(token);
            return getUserById(userId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> getUsersByRole(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, role);
        return userMapper.selectList(wrapper).stream()
                .map(this::sanitizeUser)
                .toList();
    }

    // 简化：直接返回用户ID
    public Long parseToken(String token) {
        try {
            return Long.parseLong(token);
        } catch (Exception e) {
            return null;
        }
    }

    private User sanitizeUser(User user) {
        User sanitized = new User();
        sanitized.setId(user.getId());
        sanitized.setUsername(user.getUsername());
        sanitized.setNickname(user.getNickname());
        sanitized.setRole(user.getRole());
        sanitized.setAvatar(user.getAvatar());
        sanitized.setCreatedAt(user.getCreatedAt());
        return sanitized;
    }
}
