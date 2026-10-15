package com.josbin.phishguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id")
    private PhishTemplate template;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private AdminUser creator;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
        mappedBy = "campaign",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @OrderBy("id ASC")
    private List<Target> targets = new ArrayList<>();

    public Campaign() {
    }

    public Campaign(String name, PhishTemplate template, AdminUser creator) {
        this.id = System.nanoTime();
        this.name = name;
        this.template = template;
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PhishTemplate getTemplate() {
        return template;
    }

    public AdminUser getCreator() {
        return creator;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setName(String v) {
        name = v;
    }

    public void setTemplate(PhishTemplate v) {
        template = v;
    }

    public void setCreator(AdminUser v) {
        creator = v;
    }

    public void setStatus(String v) {
        status = v;
    }
}
