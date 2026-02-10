package com.letsplay.user;

import com.letsplay.exception.NotFoundException;
import com.letsplay.user.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) { this.repo = repo; }

    public List<UserResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse get(String id) {
        User u = repo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(u);
    }

    public void delete(String id) {
        User u = repo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        repo.delete(u);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name());
    }
}
