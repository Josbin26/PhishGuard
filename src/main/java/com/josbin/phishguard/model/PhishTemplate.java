package com.josbin.phishguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "templates")
public class PhishTemplate {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(length = 20)
    private String difficulty = "medium";

    @Column(name = "email_subject", length = 200)
    private String emailSubject;

    @Lob
    @Column(name = "email_body")
    private String emailBody;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "template")
    private List<Campaign> campaigns = new ArrayList<>();

    public PhishTemplate() {
    }

    public PhishTemplate(String name, String slug, String difficulty, String emailSubject) {
        this.id = System.nanoTime();
        this.name = name;
        this.slug = slug;
        this.difficulty = difficulty;
        this.emailSubject = emailSubject;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setName(String v) {
        name = v;
    }

    public void setSlug(String v) {
        slug = v;
    }

    public void setDifficulty(String v) {
        difficulty = v;
    }

    public void setEmailSubject(String v) {
        emailSubject = v;
    }

    public void setEmailBody(String v) {
        emailBody = v;
    }
}
