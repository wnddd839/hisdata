package com.his.server.dto;

import lombok.Data;

@Data
public class MedicalRecordDTO {
    private Integer pid;
    private Integer doctorId;
    private Integer appointmentId;
    private String chiefComplaint;
    private String presentIllness;
    private String physicalExamination;
    private String preliminaryDiagnosis;
    private String treatmentPlan;
    private String notes;
}