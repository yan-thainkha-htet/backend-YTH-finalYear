package com.hospital.irrewaddy.dto;

public class SetupStatusResponse {
    private boolean profileCompleted;
    private boolean emailVerified;
    private boolean passwordChanged;
    private boolean setupComplete;

    // Getters and Setters
    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    public boolean isSetupComplete() {
        return setupComplete;
    }

    public void setSetupComplete(boolean setupComplete) {
        this.setupComplete = setupComplete;
    }
}