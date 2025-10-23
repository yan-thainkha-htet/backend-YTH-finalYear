package com.hospital.irrewaddy.config;

import com.hospital.irrewaddy.model.Department;
import com.hospital.irrewaddy.model.Receptionist;
import com.hospital.irrewaddy.model.User;
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
    private DepartmentRepository departmentRepository;

    @Autowired
    private ReceptionistRepository receptionistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin if not exists
        if (!userRepository.existsByRole(User.UserRole.ADMIN)) {
            User admin = new User();
            admin.setUsername("admin@hospital.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setEmail("hlyanhtet442@gmail.com");
            admin.setPhone("09294285689");
            admin.setFullName("Super Admin");
            admin.setRole(User.UserRole.ADMIN);
            admin.setIsActive(true);
            admin.setMustChangePassword(true); // ← Force password change
            userRepository.save(admin);
            System.out.println("✅ Admin user created: admin@hospital.com / Admin@123");
            System.out.println("⚠️  IMPORTANT: Admin must change password on first login!");
        }

        // Create sample receptionist if not exists
        if (!userRepository.existsByRole(User.UserRole.RECEPTIONIST)) {
            User receptionistUser = new User();
            receptionistUser.setUsername("receptionist_222");
            receptionistUser.setPasswordHash(passwordEncoder.encode("Reception@123"));
            receptionistUser.setEmail("receptionist22@gmail.com");
            receptionistUser.setPhone("8888888888");
            receptionistUser.setFullName("Emma Wilson");
            receptionistUser.setRole(User.UserRole.RECEPTIONIST);
            receptionistUser.setIsActive(true);
            receptionistUser.setMustChangePassword(true); // ← Force password change
            receptionistUser = userRepository.save(receptionistUser);

            Receptionist receptionist = new Receptionist();
            receptionist.setUser(receptionistUser);
            receptionist.setEmployeeId("REC001");
            receptionist.setShift("MORNING");
            receptionist.setDeskNumber(1);
            receptionist.setIsOnDuty(true);
            receptionistRepository.save(receptionist);

            System.out.println("✅ Receptionist created: receptionist@hospital.com / Reception@123");
            System.out.println("⚠️  IMPORTANT: Receptionist must change password on first login!");
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
                dept.setCapacity(capacities[i]);
                dept.setIsActive(true);
                departmentRepository.save(dept);
            }
            System.out.println("✅ Sample departments created");
        }
    }
}