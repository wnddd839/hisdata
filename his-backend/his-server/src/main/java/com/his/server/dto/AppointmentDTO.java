package com.his.server.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentDTO {
    private Integer pid;
    private Integer userId;

    @NotNull(message = "医生ID不能为空")
    private Integer doctorId;

    @NotBlank(message = "科室不能为空")
    private String department;

    /**
     * @deprecated 排班系统已移除,所有医生默认任何时刻都可挂号
     */
    @Deprecated
    private Integer scheduleId;

    private String cardNumber;

    private LocalDate registrationDate;

    @NotNull(message = "挂号费不能为空")
    @DecimalMin(value = "0.01", message = "挂号费必须大于0")
    private BigDecimal registrationFee;

    private LocalTime registrationTime;
}
