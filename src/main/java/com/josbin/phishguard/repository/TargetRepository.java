package com.josbin.phishguard.repository;
import com.josbin.phishguard.model.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TargetRepository extends JpaRepository<Target,Long>{Optional<Target> findByTrackingToken(String token);}
