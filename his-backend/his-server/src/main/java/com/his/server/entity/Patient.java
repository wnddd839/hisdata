package com.his.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ai_patient", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_card_number", columnList = "card_number"),
    @Index(name = "idx_is_deleted", columnList = "is_deleted")
})
@SQLDelete(sql = "UPDATE ai_patient SET is_deleted = 1 WHERE pid = ?")
@Where(clause = "is_deleted = 0")
public class Patient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pid")
    private Integer pid;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer gender; // 1=男, 2=女

    @Column(nullable = false)
    private Integer age;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "card_number", length = 50, unique = true)
    private String cardNumber;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "allergy", columnDefinition = "TEXT") // 缩写
    private String allergy;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;
}
