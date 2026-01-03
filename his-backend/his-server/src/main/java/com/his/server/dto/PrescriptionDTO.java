package com.his.server.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PrescriptionDTO {
    @NotNull(message = "患者ID不能为空")
    private Integer pid;

    @NotNull(message = "医生ID不能为空")
    private Integer doctorId;

    @NotNull(message = "挂号单ID不能为空")
    private Integer appointmentId;

    @NotNull(message = "药品ID不能为空")
    private Integer medicineId;

    @NotBlank(message = "剂量不能为空")
    private String dosage;

    @NotBlank(message = "剂量单位不能为空")
    private String dosageUnit;

    @NotBlank(message = "用药频率不能为空")
    private String frequency;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}