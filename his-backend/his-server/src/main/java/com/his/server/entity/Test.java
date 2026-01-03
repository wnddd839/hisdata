package com.his.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tests", indexes = {
    @Index(name = "idx_patient_date", columnList = "pid, test_date"),
    @Index(name = "idx_doctor_type", columnList = "doctor_id, test_type"),
    @Index(name = "idx_status_date", columnList = "status, test_date")
})
public class Test extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Integer testId;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    /**
     * @deprecated 使用 {@link #pid} 代替
     * 仅用于数据库兼容性,API响应中不会返回此字段
     */
    @Deprecated
    @Column(name = "patient_id", nullable = false)
    @JsonIgnore
    private Integer patientId;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(name = "appointment_id", nullable = false)
    private Integer appointmentId;

    @Column(name = "test_type", nullable = false)
    private Integer testType; // 1=血常规, 2=尿常规, 3=B超, 4=CT

    @Column(name = "test_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal testFee;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(nullable = false)
    private Integer status = 0; // 0=未支付, 1=待检查, 2=已完成

    @PrePersist
    @PreUpdate
    public void syncPid() {
        if (this.pid == null && this.patientId != null) {
            this.pid = this.patientId;
        }
        if (this.patientId == null && this.pid != null) {
            this.patientId = this.pid;
        }
    }
}
