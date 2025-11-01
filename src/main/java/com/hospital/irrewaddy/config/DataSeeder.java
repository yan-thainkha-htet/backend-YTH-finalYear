package com.hospital.irrewaddy.config;

import com.hospital.irrewaddy.model.Admin;
import com.hospital.irrewaddy.model.Department;
import com.hospital.irrewaddy.model.Receptionist;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.AdminRepository;
import com.hospital.irrewaddy.repository.DepartmentRepository;
import com.hospital.irrewaddy.repository.ReceptionistRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ReceptionistRepository receptionistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin if not exists
        if (!userRepository.existsByRole(User.UserRole.SUPER_ADMIN)) {
            User user = new User();
            user.setFullName("Super Admin");
            user.setUsername("superadmin@hospital");
            user.setPasswordHash(passwordEncoder.encode("Superadmin@123"));
            user.setRole(User.UserRole.SUPER_ADMIN);
            user.setIsActive(true);
            user.setMustChangePassword(true);
            user.setIsProfileCompleted(false);
            user.setIsEmailVerified(false);// ← Force password change
            userRepository.save(user);

            Admin admin = new Admin();
            admin.setUser(user);
            admin = adminRepository.save(admin);
            //System.out.println("✅ Admin user created: admin@hospital.com / Admin@123");
            //System.out.println("⚠️  IMPORTANT: Admin must change password on first login!");
        }

        // Create sample departments if not exist
        if (departmentRepository.count() == 0) {
            String[] deptNames = {"Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Dermatology"};
            String[] deptDescs = {
                    "Heart and cardiovascular diseases",
                    "Nervous system disorders and brain health",
                    "Bones, joints, and musculoskeletal system",
                    "Children's healthcare and development",
                    "Skin, hair, and nail conditions"
            };
            int[] capacities = {50, 30, 40, 35, 25};

            for (int i = 0; i < deptNames.length; i++) {
                Department dept = new Department();
                dept.setName(deptNames[i]);
                dept.setDescription(deptDescs[i]);
                dept.setIsActive(true);
                departmentRepository.save(dept);
            }
            System.out.println("✅ Sample departments created");
        }
    }
}