package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Doctor;
import com.his.server.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "医生管理")
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "查询医生列表(可按科室筛选)")
    @GetMapping
    public GlobalResult<List<Doctor>> list(@RequestParam(value = "department", required = false) String department) {
        return GlobalResult.success(doctorService.list(department));
    }

    @Operation(summary = "查询医生详情")
    @GetMapping("/{doctorId}")
    public GlobalResult<Map<String, Object>> getDetail(@PathVariable("doctorId") Integer doctorId) {
        return GlobalResult.success(doctorService.getDoctorDetail(doctorId));
    }

    @Operation(summary = "查询医生统计信息")
    @GetMapping("/{doctorId}/statistics")
    public GlobalResult<Map<String, Object>> getStatistics(@PathVariable("doctorId") Integer doctorId) {
        return GlobalResult.success(doctorService.getDoctorStatistics(doctorId));
    }

    @Operation(summary = "添加医生")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<Doctor> save(@RequestBody Doctor doctor) {
        return GlobalResult.success(doctorService.save(doctor));
    }
}
