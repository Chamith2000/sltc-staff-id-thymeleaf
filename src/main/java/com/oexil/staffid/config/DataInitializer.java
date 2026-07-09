package com.oexil.staffid.config;

import com.oexil.staffid.enums.ERole;
import com.oexil.staffid.model.masters.Role;
import com.oexil.staffid.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Created ROLE_ADMIN");
        }

        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            Role userRole = new Role(ERole.ROLE_USER);
            roleRepository.save(userRole);
            System.out.println("Created ROLE_USER");
        }

        if (roleRepository.findByName(ERole.ROLE_MANAGER).isEmpty()) {
            Role managerRole = new Role(ERole.ROLE_MANAGER);
            roleRepository.save(managerRole);
            System.out.println("Created ROLE_MANAGER");
        }
    }
}