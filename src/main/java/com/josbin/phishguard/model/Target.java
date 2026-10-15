package com.josbin.phishguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "targets",
    uniqueConstraints = @UniqueConstraint(columnNames = "tracking_token")
)
public class Target {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Column(length = 200)
    private String email = "";

    @Column(
        name = "tracking_token",
        nullable = false,
        unique = true,
        length = 64
    )
    private String trackingToken =
        UUID.randomUUID().toString().replace("-", "");

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
        mappedBy = "target",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @OrderBy("timestamp ASC")
    private List<Event> events = new ArrayList<>();

    public Target() {
    }

    public Target(Campaign campaign, String email) {
        this.id = System.nanoTime();
        this.campaign = campaign;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public String getEmail() {
        return email;
    }

    public String getTrackingToken() {
        return trackingToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setCampaign(Campaign v) {
        campaign = v;
    }

    public void setEmail(String v) {
        email = v;
    }

    public void setTrackingToken(String v) {
        trackingToken = v;
    }
}
