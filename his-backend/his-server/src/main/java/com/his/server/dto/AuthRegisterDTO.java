package com.his.server.dto;

import lombok.Data;

@Data
public class AuthRegisterDTO {
    private String phone;
    private String password;
    private String name;
    private Integer gender;
    private Integer age;
    private String address;
    private String allergy;
}

