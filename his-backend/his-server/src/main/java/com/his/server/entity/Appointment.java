package com.his.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_reg_date_dept_status", columnList = "registration_date, department, status"),
    @Index(name = "idx_patient_reg_date", columnList = "pid, registration_date"),
    @Index(name = "idx_doctor_status", columnList = "doctor_id, status")
})
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointmentId;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "registration_time", nullable = false)
    private LocalTime registrationTime;

    @Column(name = "registration_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal registrationFee;

    @Column(nullable = false)
    private Integer status; // 1=待就诊, 2=就诊中, 3=已完成

    @Column(name = "call_number_time")
    private LocalDateTime callNumberTime;

    @Column(name = "consult_start_time")
    private LocalDateTime consultStartTime;

    @Column(name = "consult_end_time")
    private LocalDateTime consultEndTime;

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
