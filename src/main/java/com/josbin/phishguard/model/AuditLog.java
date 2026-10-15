package com.josbin.phishguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private AdminUser admin;

    @Column(length = 200)
    private String action;

    @Lob
    private String detail = "";

    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {
    }

    public AuditLog(AdminUser admin, String action, String detail) {
        this.id = System.nanoTime();
        this.admin = admin;
        this.action = action;
        this.detail = detail;
    }
}
