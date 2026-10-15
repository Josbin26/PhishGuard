package com.josbin.phishguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_id")
    private Target target;

    @Column(
        name = "event_type",
        nullable = false,
        length = 50
    )
    private String eventType;

    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress = "";

    @Column(name = "user_agent", length = 300)
    private String userAgent = "";

    public Event() {
    }

    public Event(Target target, String type, String ip, String ua) {
        this.id = System.nanoTime();
        this.target = target;
        this.eventType = type;
        this.ipAddress = ip;
        this.userAgent = ua;
    }

    public Long getId() {
        return id;
    }

    public Target getTarget() {
        return target;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
