package com.his.server.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCallbackDTO {
    private Integer financeId;
    private String status; // SUCCESS, FAIL
    private String transactionId;
    private BigDecimal amount; // 支付金额,用于验证
    private String signature; // 签名,用于验证来源
    private Long timestamp; // 时间戳,防止重放攻击
}
