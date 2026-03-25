package com.cuoiky.Nhom13.repository;

import com.cuoiky.Nhom13.model.User;
import com.cuoiky.Nhom13.model.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findDistinctByRoles_NameOrderByUsernameAsc(ERole role);
    List<User> findAllByOrderByUsernameAsc();
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
