package com.cuoiky.Nhom13.config;

import com.cuoiky.Nhom13.dto.JobRequest;
import com.cuoiky.Nhom13.dto.JobAssignmentRequest;
import com.cuoiky.Nhom13.model.ERole;
import com.cuoiky.Nhom13.model.JobPriority;
import com.cuoiky.Nhom13.model.JobStatus;
import com.cuoiky.Nhom13.model.Role;
import com.cuoiky.Nhom13.model.User;
import com.cuoiky.Nhom13.repository.JobRepository;
import com.cuoiky.Nhom13.repository.RoleRepository;
import com.cuoiky.Nhom13.repository.UserRepository;
import com.cuoiky.Nhom13.service.JobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(2)
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobRepository jobRepository;
    private final JobService jobService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataSeeder(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      JobRepository jobRepository,
                      JobService jobService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobRepository = jobRepository;
        this.jobService = jobService;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || jobRepository.count() > 0) {
            return;
        }

        User admin = createUserIfMissing("dispatcher", "dispatcher@fsm.local", "123456", Set.of(ERole.ROLE_ADMIN));
        User tech1 = createUserIfMissing("tech01", "tech01@fsm.local", "123456", Set.of(ERole.ROLE_USER));
        User tech2 = createUserIfMissing("tech02", "tech02@fsm.local", "123456", Set.of(ERole.ROLE_USER));

        Long job1 = createJob("Install Fiber Router", "Khảo sát và lắp router cho khách hàng VIP",
                "Nguyen Van A", "12 Nguyen Hue, District 1", LocalDate.now().plusDays(1), JobPriority.HIGH);
        Long job2 = createJob("Repair Air Conditioner", "Kiểm tra block máy và thay cảm biến",
                "Tran Thi B", "88 Le Loi, District 3", LocalDate.now().plusDays(2), JobPriority.MEDIUM);
        Long job3 = createJob("Electrical Inspection", "Kiểm tra tủ điện định kỳ tại nhà máy",
                "Factory C", "Lot B2, Thu Duc", LocalDate.now().plusDays(3), JobPriority.URGENT);

        assignAndMove(job1, tech1.getId(), admin.getUsername(), JobStatus.IN_PROGRESS);
        assignAndMove(job2, tech2.getId(), admin.getUsername(), JobStatus.COMPLETED);
        assignAndMove(job3, tech1.getId(), admin.getUsername(), JobStatus.ASSIGNED);
    }

    private User createUserIfMissing(String username, String email, String rawPassword, Set<ERole> roleNames) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRoles(resolveRoles(roleNames));
            return userRepository.save(user);
        });
    }

    private Set<Role> resolveRoles(Set<ERole> roleNames) {
        Set<Role> roles = new HashSet<>();
        roleNames.forEach(roleName -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
            roles.add(role);
        });
        return roles;
    }

    private Long createJob(String title, String description, String customerName, String address,
                           LocalDate scheduledDate, JobPriority priority) {
        JobRequest request = new JobRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCustomerName(customerName);
        request.setServiceAddress(address);
        request.setScheduledDate(scheduledDate);
        request.setPriority(priority);
        return jobService.create(request).getId();
    }

    private void assignAndMove(Long jobId, Long technicianId, String dispatcherUsername, JobStatus targetStatus) {
        JobAssignmentRequest assignRequest = new JobAssignmentRequest();
        assignRequest.setTechnicianId(technicianId);
        assignRequest.setNote("Seed assignment");
        jobService.assign(jobId, assignRequest, dispatcherUsername);

        if (targetStatus == JobStatus.ASSIGNED) {
            return;
        }

        String technicianUsername = userRepository.findById(technicianId)
                .orElseThrow(() -> new IllegalStateException("Technician not found"))
                .getUsername();

        if (targetStatus == JobStatus.IN_PROGRESS || targetStatus == JobStatus.COMPLETED) {
            jobService.updateStatus(jobId, JobStatus.IN_PROGRESS, technicianUsername, false);
        }
        if (targetStatus == JobStatus.COMPLETED) {
            jobService.updateStatus(jobId, JobStatus.COMPLETED, technicianUsername, false);
        }
    }
}
