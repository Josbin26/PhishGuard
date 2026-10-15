package com.josbin.phishguard.repository;
import com.josbin.phishguard.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CampaignRepository extends JpaRepository<Campaign,Long>{List<Campaign> findAllByOrderByCreatedAtDesc();}
