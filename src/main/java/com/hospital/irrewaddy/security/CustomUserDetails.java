package com.hospital.irrewaddy.security;

import com.hospital.irrewaddy.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private Long id;
    private String email;
    private String username;
    private String passwordHash;
    private User.UserRole role;
    private Boolean isActive;
    private Boolean isProfileCompleted;
    private Boolean isEmailVerified;
    private Boolean mustChangePassword;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.isActive = user.getIsActive();
        this.isProfileCompleted = user.getIsProfileCompleted();
        this.isEmailVerified = user.getIsEmailVerified();
        this.mustChangePassword = user.getMustChangePassword();
    }

    // Custom getters
    public Long getId() {
        return id;
    }

    public Boolean isProfileCompleted() {
        return isProfileCompleted;
    }

    public Boolean isEmailVerified() {
        return isEmailVerified;
    }

    public Boolean mustChangePassword() {
        return mustChangePassword;
    }

    public User.UserRole getUserRole() {
        return role;
    }

    public String getRoleAsString() {
        return role != null ? role.name() : null;
    }

    // UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role != null) {
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        // Return email as username for authentication
        return email != null ? email : username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // If user must change password, credentials are considered expired
        //return mustChangePassword != null ? !mustChangePassword : true;
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null ? isActive : false;
    }
}