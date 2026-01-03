package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.TestDTO;
import com.his.server.entity.Test;
import com.his.server.service.PatientService;
import com.his.server.service.TestService;
import com.his.server.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "检查管理")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final PatientService patientService;

    @Operation(summary = "申请检查")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Test> create(@RequestBody TestDTO dto) {
        return GlobalResult.success(testService.createTest(dto));
    }

    @Operation(summary = "申请检查（前端专用路径）")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Test> createWithPath(@RequestBody TestDTO dto) {
        return create(dto);
    }

    @Operation(summary = "查询患者检查")
    @GetMapping("/patient/{pid}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<List<Test>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(testService.listByPatient(pid));
    }

    @Operation(summary = "查询挂号单检查")
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<List<Test>> listByAppointment(@PathVariable("appointmentId") Integer appointmentId) {
        return GlobalResult.success(testService.listByAppointment(appointmentId));
    }

    @Operation(summary = "查询待检查列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<List<Test>> listPending() {
        return GlobalResult.success(testService.listPending());
    }

    @Operation(summary = "查询医生的待检查列表")
    @GetMapping("/doctor-pending")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<List<Test>> listDoctorPending(@RequestParam("doctorId") Integer doctorId) {
        return GlobalResult.success(testService.listPendingByDoctor(doctorId));
    }

    @Operation(summary = "查询医生的所有检查列表")
    @GetMapping("/doctor-all")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<List<Test>> listDoctorAll(@RequestParam("doctorId") Integer doctorId) {
        return GlobalResult.success(testService.listByDoctor(doctorId));
    }

    @Operation(summary = "更新检查状态/结果")
    @PutMapping("/{testId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<Test> updateStatus(@PathVariable("testId") Integer testId,
                                           @RequestParam("status") Integer status,
                                           @RequestParam(value = "result", required = false) String result) {
        return GlobalResult.success(testService.updateStatus(testId, status, result));
    }

    @Operation(summary = "查询我的未支付检查")
    @GetMapping("/my-unpaid")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> listMyUnpaid() {
        return GlobalResult.success(testService.listMyUnpaid());
    }

    @Operation(summary = "查询我的检查（前端专用接口）")
    @GetMapping("/my-tests")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Test>> listMyTests() {
        // 获取当前患者pid
        var patient = patientService.getCurrentPatient(SecurityUtils.getCurrentUserId());
        return GlobalResult.success(testService.listByPatient(patient.getPid()));
    }

    @Operation(summary = "查询我的检查结果（前端专用接口）")
    @GetMapping("/my-results")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Test>> listMyResults() {
        // 获取当前患者pid
        var patient = patientService.getCurrentPatient(SecurityUtils.getCurrentUserId());
        // 返回已有结果的检查（status >= 2）
        List<Test> allTests = testService.listByPatient(patient.getPid());
        List<Test> results = allTests.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() >= 2)
                .toList();
        return GlobalResult.success(results);
    }
}
