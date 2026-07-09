package com.oexil.staffid.service.impl;

import com.oexil.staffid.model.User;
import com.oexil.staffid.repository.UserRepository;
import com.oexil.staffid.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserImpl implements CurrentUser {

    private final UserRepository userRepository;

    @Override
    public User getUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @Override
    public User getUserForGlobalControllerAdvice() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).orElseThrow();
        } else {
            return null;
        }
    }
}
