package com.josbin.phishguard.config;

import com.josbin.phishguard.model.*;
import com.josbin.phishguard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean CommandLineRunner seed(AdminUserRepository admins, PhishTemplateRepository templates, PasswordEncoder encoder){
        return args -> {
            if(admins.findByEmail("admin@phishguard.local").isEmpty())
                admins.save(new AdminUser("Admin","admin@phishguard.local",encoder.encode("admin123"),"admin"));
            seedTemplate(templates,"Microsoft 365 Login","ms365","medium","Action Required: Verify your account");
            seedTemplate(templates,"IT Helpdesk Password Reset","helpdesk","hard","Action Required: Your network password expires in 24 hours");
            seedTemplate(templates,"HR Employee Portal","hr_portal","medium","HR Portal: Your payslip and appraisal records are ready");
        };
    }
    private void seedTemplate(PhishTemplateRepository r,String name,String slug,String difficulty,String subject){
        if(r.findBySlug(slug).isEmpty()) r.save(new PhishTemplate(name,slug,difficulty,subject));
    }
}
