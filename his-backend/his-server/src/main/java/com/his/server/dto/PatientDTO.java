package com.his.server.dto;

import lombok.Data;

@Data
public class PatientDTO {
    private String name;
    private Integer gender;
    private Integer age;
    private String phone;
    private String address;
    private String allergy;
    private String idCard;
    private String medicalHistory;
}
