package com.buseasy.controller;

import com.buseasy.model.User;
import com.buseasy.service.AuthService;
import com.buseasy.view.profile.ProfilePanel;

/**
 * Connects ProfilePanel events to AuthService.
 */
public class ProfileController {

    private final AuthService  authService;
    private final ProfilePanel profilePanel;

    private final User currentUser;

    public ProfileController(User currentUser, ProfilePanel profilePanel) {
        this.authService  = new AuthService();
        this.currentUser  = currentUser;
        this.profilePanel = profilePanel;
    }

    /** Displays the current user's data in the profile panel. */
    public void loadProfile() {
        profilePanel.renderUser(currentUser);
    }

    /** Saves updated profile fields entered by the user. */
    public void saveProfile(String fullName, String email,
                             String phone, boolean isMilitary) {
        try {
            User updatedUser = authService.updateProfile(currentUser, fullName, email, phone, isMilitary);
            applyUpdatedProfile(updatedUser);
            profilePanel.renderUser(currentUser);
            profilePanel.showSuccess("Profile saved.");
        } catch (IllegalArgumentException e) {
            profilePanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            profilePanel.showError("Could not save profile: " + e.getMessage());
        }
    }

    private void applyUpdatedProfile(User updatedUser) {
        currentUser.setFullName(updatedUser.getFullName());
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setPhone(updatedUser.getPhone());
        currentUser.setMilitary(updatedUser.isMilitary());
    }
}
