package com.oexil.staffid.service;

import com.oexil.staffid.dto.user.UserAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    ResponseEntity<?> authenticateUser(UserAuth login);
}
