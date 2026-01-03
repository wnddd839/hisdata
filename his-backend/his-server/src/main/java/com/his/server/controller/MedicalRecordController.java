package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.MedicalRecordDTO;
import com.his.server.entity.MedicalRecord;
import com.his.server.service.MedicalRecordService;
import com.his.server.service.PatientService;
import com.his.server.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "病历管理")
@RestController
@RequestMapping("/api/medical-record")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;

    @Operation(summary = "创建病历")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<MedicalRecord> create(@RequestBody MedicalRecordDTO dto) {
        return GlobalResult.success(medicalRecordService.createMedicalRecord(dto));
    }

    @Operation(summary = "创建病历（前端专用路径）")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<MedicalRecord> createWithPath(@RequestBody MedicalRecordDTO dto) {
        return create(dto);
    }

    @Operation(summary = "更新病历")
    @PutMapping("/{recordId}")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<MedicalRecord> update(
            @PathVariable("recordId") Integer recordId,
            @RequestBody MedicalRecordDTO dto) {
        return GlobalResult.success(medicalRecordService.updateMedicalRecord(recordId, dto));
    }

    @Operation(summary = "删除病历")
    @DeleteMapping("/{recordId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<Void> delete(@PathVariable("recordId") Integer recordId) {
        medicalRecordService.deleteMedicalRecord(recordId);
        return GlobalResult.success();
    }

    @Operation(summary = "查询患者病历")
    @GetMapping("/patient/{pid}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<List<MedicalRecord>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(medicalRecordService.listByPatient(pid));
    }

    @Operation(summary = "查询挂号单病历")
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<List<MedicalRecord>> listByAppointment(@PathVariable("appointmentId") Integer appointmentId) {
        return GlobalResult.success(medicalRecordService.listByAppointment(appointmentId));
    }

    @Operation(summary = "查询患者历史病历")
    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<List<MedicalRecord>> history(@RequestParam("pid") Integer pid) {
        return GlobalResult.success(medicalRecordService.listByPatient(pid));
    }

    @Operation(summary = "查询我的病历")
    @GetMapping("/my-records")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<MedicalRecord>> listMyRecords() {
        var patient = patientService.getCurrentPatient(SecurityUtils.getCurrentUserId());
        return GlobalResult.success(medicalRecordService.listByPatient(patient.getPid()));
    }
}
