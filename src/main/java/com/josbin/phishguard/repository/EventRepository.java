package com.josbin.phishguard.repository;
import com.josbin.phishguard.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EventRepository extends JpaRepository<Event,Long>{}
