package com.josbin.phishguard.repository;
import com.josbin.phishguard.model.PhishTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PhishTemplateRepository extends JpaRepository<PhishTemplate,Long>{Optional<PhishTemplate> findBySlug(String slug);}
