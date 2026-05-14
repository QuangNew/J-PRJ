package com.buseasy.service;

import java.sql.SQLException;

import com.buseasy.dao.UserDao;
import com.buseasy.model.User;
import com.buseasy.util.PasswordUtil;

/**
 * Handles user registration and login.
 * All password hashing happens here — never in a DAO or View.
 */
public class AuthService {

    private final UserDao userDao = new UserDao();

    /**
     * Validates credentials and returns the authenticated User.
     *
     * @throws IllegalArgumentException if username or password is wrong
     * @throws RuntimeException         if a database error occurs
     */
    public User login(String username, String rawPassword) {
        String normalizedUsername = requireText(username, "Username");
        String normalizedPassword = requireText(rawPassword, "Password");

        try {
            User user = userDao.findByUsername(normalizedUsername);
            if (user == null || !PasswordUtil.matches(normalizedPassword, user.getPasswordHash())) {
                throw new IllegalArgumentException("Incorrect username or password.");
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Database error during login.", e);
        }
    }

    /**
     * Creates a new user account.
     *
     * @throws IllegalArgumentException if the username or email is already taken,
     *                                  or if required fields are empty
     * @throws RuntimeException         if a database error occurs
     */
    public User register(String username, String rawPassword,
                         String fullName, String email,
                         String phone, boolean isMilitary) {
        String normalizedUsername = requireText(username, "Username");
        String normalizedPassword = requireText(rawPassword, "Password");
        String normalizedFullName = requireText(fullName, "Full name");
        String normalizedEmail = requireText(email, "Email");
        String normalizedPhone = normalizeOptional(phone);

        try {
            if (userDao.findByUsername(normalizedUsername) != null) {
                throw new IllegalArgumentException("Username '" + normalizedUsername + "' is already taken.");
            }
            if (userDao.findByEmail(normalizedEmail) != null) {
                throw new IllegalArgumentException("Email '" + normalizedEmail + "' is already registered.");
            }
            User newUser = new User();
            newUser.setUsername(normalizedUsername);
            newUser.setPasswordHash(PasswordUtil.hash(normalizedPassword));
            newUser.setFullName(normalizedFullName);
            newUser.setEmail(normalizedEmail);
            newUser.setPhone(normalizedPhone);
            newUser.setMilitary(isMilitary);
            newUser.setRole("USER");
            userDao.insert(newUser);
            return newUser;
        } catch (SQLException e) {
            throw new RuntimeException("Database error during registration.", e);
        }
    }

    /**
     * Loads a user by id — used when auto-logging in from a saved session.
     *
     * @return the User, or null if the id does not exist
     */
    public User loadUserById(int userId) {
        try {
            return userDao.findById(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error loading user.", e);
        }
    }

    /**
     * Updates the profile fields of an existing user.
     */
    public User updateProfile(User user, String fullName, String email,
                               String phone, boolean isMilitary) {
        String normalizedFullName = requireText(fullName, "Full name");
        String normalizedEmail = requireText(email, "Email");
        String normalizedPhone = normalizeOptional(phone);

        try {
            User existingUser = userDao.findByEmail(normalizedEmail);
            if (existingUser != null && existingUser.getId() != user.getId()) {
                throw new IllegalArgumentException("Email '" + normalizedEmail + "' is already registered.");
            }

            User updatedUser = copyUser(user);
            updatedUser.setFullName(normalizedFullName);
            updatedUser.setEmail(normalizedEmail);
            updatedUser.setPhone(normalizedPhone);
            updatedUser.setMilitary(isMilitary);

            userDao.update(updatedUser);
            return updatedUser;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating profile.", e);
        }
    }

    private User copyUser(User user) {
        return new User(
            user.getId(),
            user.getUsername(),
            user.getPasswordHash(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            user.isMilitary(),
            user.getRole(),
            user.getCreatedAt()
        );
    }

    private String requireText(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
