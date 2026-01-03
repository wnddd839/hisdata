package com.his.server.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "login_logs", indexes = {
    @Index(name = "idx_user_time", columnList = "user_id, login_time")
})
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "username")
    private String username;

    @Column(name = "role")
    private String role;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "status")
    private String status; // SUCCESS, FAIL

    @Column(name = "message")
    private String message;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;
}
