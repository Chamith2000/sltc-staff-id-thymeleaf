package com.oexil.staffid.security;

import com.oexil.staffid.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService, CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                // CSRF configuration
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/png-generation/**") // Disable CSRF for PNG generation API
                )
                // Session-based authentication
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))

                // Define access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/vendors/**", "/files/**").permitAll() // Allow public access to login and static resources
                        .requestMatchers("/public/student/**").permitAll() // Allow public access to student
                        .requestMatchers("/api/png-generation/**").authenticated() // Allow authenticated access to PNG generation API
                        .requestMatchers("/staff/generate-eSignature/**").permitAll() // Allow public access for Puppeteer PNG generation
                        .anyRequest().authenticated() // Protect all other URLs
                )

                // Configure login
                .formLogin(form -> form
                        .loginPage("/auth/login") // Custom login page
                        .loginProcessingUrl("/do-login") // URL to submit login form
                        .defaultSuccessUrl("/", true) // Redirect after successful login
                        .failureHandler(customAuthenticationFailureHandler) // Use custom failure handler
                        .permitAll()
                )

                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/logout") // Custom logout URL
                        .logoutSuccessUrl("/auth/login?logout") // Redirect after logout
                        .invalidateHttpSession(true) // Invalidate session
                        .deleteCookies("JSESSIONID") // Delete session cookie
                        .permitAll()
                );
        return http.build();
    }
}
