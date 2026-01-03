package com.his.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "finance", indexes = {
    @Index(name = "idx_pid", columnList = "pid"),
    @Index(name = "idx_appt_pres", columnList = "appointment_id, prescription_id"),
    @Index(name = "idx_status_created", columnList = "payment_status, created_at"),
    @Index(name = "idx_created_total", columnList = "created_at, total_fee")
})
public class Finance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "finance_id")
    private Integer financeId;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    @Column(name = "appointment_id", nullable = false)
    private Integer appointmentId;

    @Column(name = "prescription_id")
    private Integer prescriptionId;

    @Column(name = "registration_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal registrationFee;

    @Column(name = "medicine_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal medicineFee = BigDecimal.ZERO;

    @Column(name = "test_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal testFee = BigDecimal.ZERO;

    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "fee_details", columnDefinition = "JSON")
    private String feeDetails;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "未支付";

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
}
