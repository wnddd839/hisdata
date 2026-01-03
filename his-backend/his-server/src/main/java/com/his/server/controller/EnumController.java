package com.his.server.controller;

import com.his.common.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "枚举值查询")
@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @Data
    public static class EnumItem {
        private Integer value;
        private String label;
        private String description;

        public EnumItem(Integer value, String label, String description) {
            this.value = value;
            this.label = label;
            this.description = description;
        }
    }

    @Operation(summary = "查询挂号状态枚举")
    @GetMapping("/appointment-status")
    public GlobalResult<List<EnumItem>> getAppointmentStatus() {
        List<EnumItem> items = Arrays.asList(
                new EnumItem(0, "已取消", "患者取消挂号"),
                new EnumItem(1, "待就诊", "已挂号,等待就诊"),
                new EnumItem(2, "就诊中", "医生正在接诊"),
                new EnumItem(3, "已完成", "就诊完成")
        );
        return GlobalResult.success(items);
    }

    @Operation(summary = "查询处方状态枚举")
    @GetMapping("/prescription-status")
    public GlobalResult<List<EnumItem>> getPrescriptionStatus() {
        List<EnumItem> items = Arrays.asList(
                new EnumItem(0, "未支付", "处方未支付"),
                new EnumItem(1, "待发药", "已支付,等待发药"),
                new EnumItem(2, "已发药", "已完成发药")
        );
        return GlobalResult.success(items);
    }

    @Operation(summary = "查询检查状态枚举")
    @GetMapping("/test-status")
    public GlobalResult<List<EnumItem>> getTestStatus() {
        List<EnumItem> items = Arrays.asList(
                new EnumItem(0, "未支付", "检查未支付"),
                new EnumItem(1, "待检查", "已支付,等待检查"),
                new EnumItem(2, "已完成", "检查完成,结果已出")
        );
        return GlobalResult.success(items);
    }

    @Operation(summary = "查询检查类型枚举")
    @GetMapping("/test-type")
    public GlobalResult<List<EnumItem>> getTestType() {
        List<EnumItem> items = Arrays.asList(
                new EnumItem(1, "血常规", "血液常规检查"),
                new EnumItem(2, "尿常规", "尿液常规检查"),
                new EnumItem(3, "B超", "B超检查"),
                new EnumItem(4, "CT", "CT扫描检查")
        );
        return GlobalResult.success(items);
    }

    @Operation(summary = "查询排班班次枚举")
    @GetMapping("/schedule-shift")
    public GlobalResult<List<EnumItem>> getScheduleShift() {
        List<EnumItem> items = Arrays.asList(
                new EnumItem(1, "上午", "上午班 (08:00-12:00)"),
                new EnumItem(2, "下午", "下午班 (14:00-18:00)"),
                new EnumItem(3, "晚班", "晚班 (18:00-22:00)")
        );
        return GlobalResult.success(items);
    }

    @Operation(summary = "查询排班状态枚举")
    @GetMapping("/schedule-status")
    public GlobalResult<List<EnumItem>> getScheduleStatus() {
        List<EnumItem> items = Arrays.asList(
                new EnumItem(0, "停诊", "医生停诊"),
                new EnumItem(1, "正常", "正常接诊")
        );
        return GlobalResult.success(items);
    }

    @Operation(summary = "查询用户角色枚举")
    @GetMapping("/user-role")
    public GlobalResult<List<EnumItem>> getUserRole() {
        List<EnumItem> items = Arrays.asList(
                new EnumItem(1, "患者", "ROLE_PATIENT"),
                new EnumItem(2, "医生", "ROLE_DOCTOR"),
                new EnumItem(3, "管理员", "ROLE_ADMIN")
        );
        return GlobalResult.success(items);
    }
}
