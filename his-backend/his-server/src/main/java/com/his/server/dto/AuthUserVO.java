package com.his.server.dto;

import lombok.Data;

@Data
public class AuthUserVO {
    private Integer userId;
    private Integer pid;
    private String phone;
    private String name;
    private String token;
}

