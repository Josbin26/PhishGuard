package com.josbin.phishguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
public class AdminUser {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 300)
    private String passwordHash;

    @Column(length = 50)
    private String role = "admin";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdminUser() {
    }

    public AdminUser(String name, String email, String passwordHash, String role) {
        this.id = System.nanoTime();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setName(String v) {
        name = v;
    }

    public void setEmail(String v) {
        email = v;
    }

    public void setPasswordHash(String v) {
        passwordHash = v;
    }

    public void setRole(String v) {
        role = v;
    }
}
