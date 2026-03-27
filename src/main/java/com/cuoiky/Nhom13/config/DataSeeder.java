package com.cuoiky.Nhom13.config;

import com.cuoiky.Nhom13.dto.JobAssignmentRequest;
import com.cuoiky.Nhom13.dto.JobPartUsageRequest;
import com.cuoiky.Nhom13.dto.JobRequest;
import com.cuoiky.Nhom13.model.ERole;
import com.cuoiky.Nhom13.model.JobPriority;
import com.cuoiky.Nhom13.model.JobStatus;
import com.cuoiky.Nhom13.model.Part;
import com.cuoiky.Nhom13.model.Role;
import com.cuoiky.Nhom13.model.User;
import com.cuoiky.Nhom13.repository.JobRepository;
import com.cuoiky.Nhom13.repository.PartRepository;
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
    private final PartRepository partRepository;
    private final JobService jobService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataSeeder(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      JobRepository jobRepository,
                      PartRepository partRepository,
                      JobService jobService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobRepository = jobRepository;
        this.partRepository = partRepository;
        this.jobService = jobService;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || jobRepository.count() > 0) {
            return;
        }

        // Create Users
        User admin = createUserIfMissing("dispatcher", "dispatcher@fsm.local", "123456", Set.of(ERole.ROLE_ADMIN));
        User tech1 = createUserIfMissing("tech01", "tech01@fsm.local", "123456", Set.of(ERole.ROLE_USER));
        User tech2 = createUserIfMissing("tech02", "tech02@fsm.local", "123456", Set.of(ERole.ROLE_USER));
        User tech3 = createUserIfMissing("tech03", "tech03@fsm.local", "123456", Set.of(ERole.ROLE_USER));

        // Create Parts
        Part part1 = createPartIfMissing("P-ROUTER", "Fiber Router", "pcs", 50, 10);
        Part part2 = createPartIfMissing("P-CABLE", "UTP Cable (meter)", "meter", 500, 100);
        Part part3 = createPartIfMissing("P-SENSOR", "Temperature Sensor", "pcs", 40, 5);
        Part part4 = createPartIfMissing("P-SWITCH", "Network Switch 24-port", "pcs", 25, 5);
        Part part5 = createPartIfMissing("P-MODEM", "ADSL Modem", "pcs", 60, 15);
        Part part6 = createPartIfMissing("P-BATTERY", "UPS Battery 12V", "pcs", 30, 8);
        Part part7 = createPartIfMissing("P-CONNECTOR", "RJ45 Connector", "pcs", 200, 50);
        Part part8 = createPartIfMissing("P-CABLE-FO", "Fiber Optic Cable (meter)", "meter", 300, 50);
        Part part9 = createPartIfMissing("P-ANTENNA", "WiFi Antenna", "pcs", 35, 10);
        Part part10 = createPartIfMissing("P-POWER", "Power Adapter 12V 2A", "pcs", 45, 10);

        // Create Jobs - Various statuses and priorities
        Long job1 = createJob("Install Fiber Router", "Survey and install router for VIP customer",
                "Nguyen Van A", "12 Nguyen Hue, District 1", LocalDate.now().plusDays(1), JobPriority.HIGH);
        Long job2 = createJob("Repair Air Conditioner", "Inspect compressor and replace sensor",
                "Tran Thi B", "88 Le Loi, District 3", LocalDate.now().plusDays(2), JobPriority.MEDIUM);
        Long job3 = createJob("Electrical Inspection", "Routine electrical cabinet inspection",
                "Factory C", "Lot B2, Thu Duc", LocalDate.now().plusDays(3), JobPriority.URGENT);
        Long job4 = createJob("Network Upgrade", "Upgrade office network infrastructure with new switches",
                "ABC Corporation", "456 Phan Xich Long, Phu Nhuan", LocalDate.now().plusDays(2), JobPriority.HIGH);
        Long job5 = createJob("WiFi Installation", "Install WiFi access points in hotel",
                "Grand Hotel", "88 Dong Khoi, District 1", LocalDate.now().plusDays(4), JobPriority.MEDIUM);
        Long job6 = createJob("Emergency Network Repair", "Critical network outage - immediate response required",
                "Finance Bank HQ", "200 Nguyen Trai, District 5", LocalDate.now(), JobPriority.URGENT);
        Long job7 = createJob("Server Room Cooling", "Install additional cooling system for server room",
                "Tech Startup Ltd", "10 Ton That Thiep, District 1", LocalDate.now().plusDays(5), JobPriority.LOW);
        Long job8 = createJob("Cable Replacement", "Replace damaged fiber optic cable",
                "Medical Center", "45 Nguyen Van Cu, District 5", LocalDate.now().plusDays(3), JobPriority.MEDIUM);
        Long job9 = createJob("UPS Installation", "Install UPS system for critical equipment",
                "Data Center A", "Tan Thuan EPZ", LocalDate.now().plusDays(6), JobPriority.HIGH);
        Long job10 = createJob("Routine Maintenance", "Monthly preventive maintenance check",
                "Office Building B", "120 Le Lai, District 1", LocalDate.now().plusDays(7), JobPriority.LOW);

        // Assign jobs and set different statuses
        // Job 1: IN_PROGRESS with parts used
        assignAndMove(job1, tech1.getId(), admin.getUsername(), JobStatus.IN_PROGRESS);
        usePart(job1, part1.getId(), 1, tech1.getUsername(), false, "Router installation");
        usePart(job1, part2.getId(), 20, tech1.getUsername(), false, "Cable run");

        // Job 2: COMPLETED
        assignAndMove(job2, tech2.getId(), admin.getUsername(), JobStatus.IN_PROGRESS);
        usePart(job2, part3.getId(), 2, tech2.getUsername(), false, "Sensor replacement");
        jobService.updateStatus(job2, JobStatus.COMPLETED, tech2.getUsername(), false);

        // Job 3: ASSIGNED (not started yet)
        assignAndMove(job3, tech1.getId(), admin.getUsername(), JobStatus.ASSIGNED);

        // Job 4: IN_PROGRESS with multiple parts
        assignAndMove(job4, tech3.getId(), admin.getUsername(), JobStatus.IN_PROGRESS);
        usePart(job4, part4.getId(), 2, tech3.getUsername(), false, "Switch installation");
        usePart(job4, part7.getId(), 48, tech3.getUsername(), false, "Network connectors");
        usePart(job4, part2.getId(), 150, tech3.getUsername(), false, "Network cabling");

        // Job 5: ASSIGNED to tech2
        assignAndMove(job5, tech2.getId(), admin.getUsername(), JobStatus.ASSIGNED);

        // Job 6: URGENT - IN_PROGRESS
        assignAndMove(job6, tech1.getId(), admin.getUsername(), JobStatus.IN_PROGRESS);
        usePart(job6, part4.getId(), 1, tech1.getUsername(), false, "Emergency switch replacement");

        // Job 7: CREATED (not assigned yet)
        // No assignment - stays in CREATED status

        // Job 8: COMPLETED
        assignAndMove(job8, tech2.getId(), admin.getUsername(), JobStatus.IN_PROGRESS);
        usePart(job8, part8.getId(), 50, tech2.getUsername(), false, "Fiber cable replacement");
        usePart(job8, part7.getId(), 4, tech2.getUsername(), false, "Connectors");
        jobService.updateStatus(job8, JobStatus.COMPLETED, tech2.getUsername(), false);

        // Job 9: ASSIGNED to tech3
        assignAndMove(job9, tech3.getId(), admin.getUsername(), JobStatus.ASSIGNED);

        // Job 10: IN_PROGRESS
        assignAndMove(job10, tech1.getId(), admin.getUsername(), JobStatus.IN_PROGRESS);
        usePart(job10, part3.getId(), 1, tech1.getUsername(), false, "Sensor check replacement");
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

    private Part createPartIfMissing(String partCode, String partName, String unit, int stock, int minStock) {
        return partRepository.findByPartCode(partCode).orElseGet(() -> {
            Part part = new Part();
            part.setPartCode(partCode);
            part.setPartName(partName);
            part.setUnit(unit);
            part.setStockQuantity(stock);
            part.setMinimumStockLevel(minStock);
            part.setActive(true);
            return partRepository.save(part);
        });
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

    private void usePart(Long jobId, Long partId, int quantity, String username, boolean isAdmin, String note) {
        JobPartUsageRequest request = new JobPartUsageRequest();
        request.setPartId(partId);
        request.setQuantityUsed(quantity);
        request.setNote(note);
        jobService.usePart(jobId, request, username, isAdmin);
    }
}
