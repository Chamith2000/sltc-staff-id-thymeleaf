package com.oexil.staffid.security.services;

import com.oexil.staffid.exception.AccountNotApprovedException;
import com.oexil.staffid.model.User;
import com.oexil.staffid.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username: {}", username);
        
        try {
            User user = userRepository.findByEmailAndActiveIsTrue(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found with user: " + username));
            
            logger.info("User found: {} with approval status: {}", user.getEmail(), user.getApprove());
            
            // Check approval status
            if (user.getApprove() == null) {
                logger.info("User {} has pending approval status, throwing AccountNotApprovedException", user.getEmail());
                throw new AccountNotApprovedException("Your account is pending approval. Please contact the System Administrator.");
            }
            if (user.getApprove() == false) {
                logger.info("User {} has been rejected, throwing AccountNotApprovedException", user.getEmail());
                throw new AccountNotApprovedException("Your account has been rejected. Please contact the system administrator.");
            }
            
            logger.info("User {} approved, returning UserDetails", user.getEmail());
            UserDetails userDetails = UserDetailsImpl.build(user);
            logger.info("UserDetails created successfully for user: {}", user.getEmail());
            return userDetails;
        } catch (AccountNotApprovedException e) {
            logger.info("Rethrowing AccountNotApprovedException: {}", e.getMessage());
            throw e;
        } catch (UsernameNotFoundException e) {
            logger.info("Rethrowing UsernameNotFoundException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in loadUserByUsername for user {}: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }
}
