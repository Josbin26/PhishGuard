package com.josbin.phishguard.service;

import com.josbin.phishguard.model.AdminUser;
import com.josbin.phishguard.repository.AdminUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {
    private final AdminUserRepository repo;
    public AdminUserDetailsService(AdminUserRepository repo){this.repo=repo;}
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminUser u=repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles(u.getRole().toUpperCase()).build();
    }
}
