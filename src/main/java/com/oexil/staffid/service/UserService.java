package com.oexil.staffid.service;

import com.oexil.staffid.dto.user.ForgotPassword;
import com.oexil.staffid.dto.user.ResetPassword;
import com.oexil.staffid.dto.user.UserDTO;
import com.oexil.staffid.dto.user.UserSignup;
import com.oexil.staffid.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    List<UserDTO> searchUsers(String query);
    
    User signup(UserSignup userSignup) throws Exception;
    boolean sendPasswordResetCode(ForgotPassword forgotPassword) throws Exception;
    boolean resetPassword(ResetPassword resetPassword) throws Exception;
    boolean isEmailValid(String email);
    User findByEmail(String email);
    
    // User approval management
    List<User> getPendingUsers();
    List<User> getApprovedUsers();
    List<User> getRejectedUsers();
    boolean approveUser(Long userId);
    boolean rejectUser(Long userId);
}
