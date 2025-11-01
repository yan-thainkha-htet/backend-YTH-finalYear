package com.hospital.irrewaddy.security;

import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find by username first, then by email
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User is inactive: " + username);
        }

        return new CustomUserDetails(user);
    }
}