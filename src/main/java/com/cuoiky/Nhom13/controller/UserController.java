package com.cuoiky.Nhom13.controller;

import com.cuoiky.Nhom13.dto.UserSummaryResponse;
import com.cuoiky.Nhom13.model.ERole;
import com.cuoiky.Nhom13.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> technicians() {
        List<UserSummaryResponse> users = userRepository.findDistinctByRoles_NameOrderByUsernameAsc(ERole.ROLE_USER)
                .stream()
                .map(user -> new UserSummaryResponse(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
        return ResponseEntity.ok(users);
    }
}
