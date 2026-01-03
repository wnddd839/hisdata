package com.his.server;

import com.his.server.entity.UserAccount;
import com.his.server.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@ComponentScan(basePackages = {"com.his"})
@EntityScan(basePackages = {"com.his"})
public class HisApplication implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public HisApplication(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(HisApplication.class, args);
    }

    @Override
    public void run(String... args) {
        userAccountRepository.findByPhone("admin").orElseGet(() -> {
            UserAccount admin = new UserAccount();
            admin.setPhone("admin");
            admin.setRole("ROLE_ADMIN");
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.setStatus(1);
            return userAccountRepository.save(admin);
        });
    }
}
