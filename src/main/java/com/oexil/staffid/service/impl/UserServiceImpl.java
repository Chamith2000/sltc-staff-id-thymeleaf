package com.oexil.staffid.service.impl;

import com.oexil.staffid.dto.user.ForgotPassword;
import com.oexil.staffid.dto.user.ResetPassword;
import com.oexil.staffid.dto.user.UserDTO;
import com.oexil.staffid.dto.user.UserSignup;
import com.oexil.staffid.enums.AuthProvider;
import com.oexil.staffid.enums.ERole;
import com.oexil.staffid.enums.UserType;
import com.oexil.staffid.model.User;
import com.oexil.staffid.model.masters.Role;
import com.oexil.staffid.repository.RoleRepository;
import com.oexil.staffid.repository.UserRepository;
import com.oexil.staffid.service.EmailService;
import com.oexil.staffid.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public List<UserDTO> searchUsers(String query) {
        List<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query);
        return users.stream().map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getFirstName() + " " + user.getLastName());
            userDTO.setEmail(user.getEmail());
            return userDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public User signup(UserSignup userSignup) throws Exception {
        if (!isEmailValid(userSignup.getEmail())) {
            throw new Exception("Email must end with @sltc.ac.lk");
        }

        if (!userSignup.getPassword().equals(userSignup.getConfirmPassword())) {
            throw new Exception("Passwords do not match");
        }

        if (userRepository.existsByEmail(userSignup.getEmail())) {
            throw new Exception("Email already exists");
        }

        if (userRepository.existsByUsername(userSignup.getEmail())) {
            throw new Exception("Email already registered");
        }

        User user = new User();
        user.setFirstName(userSignup.getFirstName());
        user.setLastName(userSignup.getLastName());
        user.setUsername(userSignup.getEmail());
        user.setEmail(userSignup.getEmail());
        user.setPassword(passwordEncoder.encode(userSignup.getPassword()));
        user.setPhoneNumber(userSignup.getPhoneNumber());
        user.setProvider(AuthProvider.LOCAL);
        user.setUserType(UserType.SYSTEM); // Default all new users to SYSTEM type
        user.setActive(true);
        user.setApprove(null); // Pending approval by default
        user.setEmailVerification(false);
        user.setTwoFactorAuth(false);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());

        // Assign roles based on user type
        Set<Role> roles = new HashSet<>();
        if (user.getUserType() == UserType.SYSTEM) {
            // System users get ROLE_ADMIN
            Optional<Role> adminRole = roleRepository.findByName(ERole.ROLE_ADMIN);
            if (adminRole.isPresent()) {
                roles.add(adminRole.get());
            } else {
                throw new Exception("Admin role not found in database");
            }
        } else {
            // Regular users get ROLE_USER
            Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_USER);
            if (userRole.isPresent()) {
                roles.add(userRole.get());
            } else {
                throw new Exception("User role not found in database");
            }
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        
        return savedUser;
    }

    @Override
    public boolean sendPasswordResetCode(ForgotPassword forgotPassword) throws Exception {
        if (!isEmailValid(forgotPassword.getEmail())) {
            throw new Exception("Email must end with @sltc.ac.lk");
        }

        Optional<User> userOptional = userRepository.findByEmailAndActiveIsTrue(forgotPassword.getEmail());
        if (userOptional.isEmpty()) {
            throw new Exception("User with this email not found");
        }

        User user = userOptional.get();
        String resetCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        user.setPasswordResetCode(resetCode);
        user.setUpdateDate(new Date());
        userRepository.save(user);

        // Send password reset email
        boolean emailSent = emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetCode);
        if (!emailSent) {
            throw new Exception("Failed to send password reset email. Please try again.");
        }

        return true;
    }

    @Override
    public boolean resetPassword(ResetPassword resetPassword) throws Exception {
        if (!resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword())) {
            throw new Exception("Passwords do not match");
        }

        Optional<User> userOptional = userRepository.findAll().stream()
                .filter(user -> resetPassword.getResetCode().equals(user.getPasswordResetCode()))
                .findFirst();

        if (userOptional.isEmpty()) {
            throw new Exception("Invalid reset code");
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(resetPassword.getNewPassword()));
        user.setPasswordResetCode(null);
        user.setUpdateDate(new Date());
        userRepository.save(user);

        return true;
    }

    @Override
    public boolean isEmailValid(String email) {
        return email != null && email.endsWith("@sltc.ac.lk");
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailAndActiveIsTrue(email).orElse(null);
    }

    @Override
    public List<User> getPendingUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getApprove() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getApprovedUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getApprove() != null && user.getApprove())
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getRejectedUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getApprove() != null && !user.getApprove())
                .collect(Collectors.toList());
    }

    @Override
    public boolean approveUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setApprove(true);
            user.setUpdateDate(new Date());
            userRepository.save(user);
            
            // Send approval email
            emailService.sendAccountApprovalEmail(user.getEmail(), user.getFullName(), true);
            
            return true;
        }
        return false;
    }

    @Override
    public boolean rejectUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setApprove(false);
            user.setUpdateDate(new Date());
            userRepository.save(user);
            
            // Send rejection email
            emailService.sendAccountApprovalEmail(user.getEmail(), user.getFullName(), false);
            
            return true;
        }
        return false;
    }
}
