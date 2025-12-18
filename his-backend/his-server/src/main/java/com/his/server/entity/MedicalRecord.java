package com.his.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "medical_records", indexes = {
    @Index(name = "idx_patient_created", columnList = "pid, created_at"),
    @Index(name = "idx_doctor_created", columnList = "doctor_id, created_at")
})
public class MedicalRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(name = "appointment_id", nullable = false)
    private Integer appointmentId;

    @Column(name = "chief_complaint", nullable = false, columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "present_illness", nullable = false, columnDefinition = "TEXT")
    private String presentIllness;

    @Column(name = "physical_examination", nullable = false, columnDefinition = "TEXT")
    private String physicalExamination;

    @Column(name = "preliminary_diagnosis", nullable = false, columnDefinition = "TEXT")
    private String preliminaryDiagnosis;

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
