package com.his.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_accounts", indexes = {
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_pid", columnList = "pid")
})
public class UserAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private Integer status = 1;
}
