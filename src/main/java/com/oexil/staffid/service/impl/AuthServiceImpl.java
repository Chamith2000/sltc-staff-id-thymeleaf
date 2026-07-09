package com.oexil.staffid.service.impl;

import com.oexil.staffid.dto.user.UserAuth;
import com.oexil.staffid.repository.UserRepository;
import com.oexil.staffid.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<?> authenticateUser(UserAuth login) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//
//            User user = userRepository.findById(userDetails.getId()).orElseThrow();
//            JwtResponseDto jwtResponseDto;
//
//            if (!user.isEmailVerification()) {
//                return createResponse();
//            }
//
//            if (user.isTwoFactorAuth()) {
//                String code = Utils.generateSixDigitsCode();
//                user.setTwoFactorAuthCode(code);
//                userRepository.save(user);
//
//                jwtResponseDto = new JwtResponseDto(userDetails.getId(), null, true, null);
//            } else {
//                loginLogoutDecisionsService.applyLoginDecisions(user);
//                userRepository.save(user);
//
//                jwtResponseDto = getJwtResponseDto(userDetails.getId(), userDetails.getUsername(), user);
//            }
//            return ResponseEntity.ok(jwtResponseDto);
//        } catch (AuthenticationException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
        return null;
    }
}
