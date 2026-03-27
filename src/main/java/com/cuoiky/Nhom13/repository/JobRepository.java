package com.cuoiky.Nhom13.repository;

import com.cuoiky.Nhom13.model.Job;
import com.cuoiky.Nhom13.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Optional<Job> findByJobCode(String jobCode);
    List<Job> findByStatus(JobStatus status);
    List<Job> findAllByOrderByIdAsc();
}
