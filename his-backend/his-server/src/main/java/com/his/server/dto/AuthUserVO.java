package com.his.server.dto;

import lombok.Data;

@Data
public class AuthUserVO {
    private Integer userId;
    private Integer pid;
    private String cardNumber; // 就诊卡号
    private String phone;
    private String name;
    private String role;
    private Integer doctorId;
    private String token;
}

