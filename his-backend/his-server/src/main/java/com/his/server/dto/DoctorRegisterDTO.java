package com.his.server.dto;

import lombok.Data;

@Data
public class DoctorRegisterDTO {
    private String name;
    private String title;
    private String department;
    private String phone;
    private String password;
}
