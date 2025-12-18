package com.his.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "prescriptions", indexes = {
    @Index(name = "idx_patient_created", columnList = "pid, created_at"),
    @Index(name = "idx_doctor_created", columnList = "doctor_id, created_at"),
    @Index(name = "idx_medicine_created", columnList = "medicine_id, created_at")
})
public class Prescription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Integer prescriptionId;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(name = "appointment_id", nullable = false)
    private Integer appointmentId;

    @Column(name = "medicine_id", nullable = false)
    private Integer medicineId;

    @Column(name = "medicine_name", nullable = false, length = 100)
    private String medicineName;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(name = "dosage_unit", nullable = false, length = 20)
    private String dosageUnit;

    @Column(nullable = false, length = 50)
    private String frequency;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

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
