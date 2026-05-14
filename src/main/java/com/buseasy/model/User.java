package com.buseasy.model;

import java.time.LocalDateTime;

public class User {

    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private boolean isMilitary;
    private String role = "USER";
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String passwordHash,
                String fullName, String email, String phone,
                boolean isMilitary, String role, LocalDateTime createdAt) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.email        = email;
        this.phone        = phone;
        this.isMilitary   = isMilitary;
        setRole(role);
        this.createdAt    = createdAt;
    }

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getUsername()               { return username; }
    public void setUsername(String username)  { this.username = username; }

    public String getPasswordHash()                       { return passwordHash; }
    public void setPasswordHash(String passwordHash)      { this.passwordHash = passwordHash; }

    public String getFullName()               { return fullName; }
    public void setFullName(String fullName)  { this.fullName = fullName; }

    public String getEmail()                  { return email; }
    public void setEmail(String email)        { this.email = email; }

    public String getPhone()                  { return phone; }
    public void setPhone(String phone)        { this.phone = phone; }

    public boolean isMilitary()               { return isMilitary; }
    public void setMilitary(boolean military) { this.isMilitary = military; }

    public String getRole()                   { return role; }
    public void setRole(String role)          { this.role = normalizeRole(role); }

    public boolean isAdmin()                  { return "ADMIN".equalsIgnoreCase(role); }

    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)     { this.createdAt = createdAt; }

    private String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            return "USER";
        }
        String normalized = value.trim().toUpperCase();
        return "ADMIN".equals(normalized) ? "ADMIN" : "USER";
    }
}
