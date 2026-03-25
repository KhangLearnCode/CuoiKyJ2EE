package com.cuoiky.Nhom13.repository;

import com.cuoiky.Nhom13.model.ERole;
import com.cuoiky.Nhom13.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
