package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.MedicalRecordDTO;
import com.his.server.entity.MedicalRecord;
import com.his.server.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "病历管理")
@RestController
@RequestMapping("/api/medical-record")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Operation(summary = "创建病历")
    @PostMapping
    public GlobalResult<MedicalRecord> create(@RequestBody MedicalRecordDTO dto) {
        return GlobalResult.success(medicalRecordService.createMedicalRecord(dto));
    }

    @Operation(summary = "查询患者病历")
    @GetMapping("/patient/{pid}")
    public GlobalResult<List<MedicalRecord>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(medicalRecordService.listByPatient(pid));
    }

    @Operation(summary = "查询挂号单病历")
    @GetMapping("/appointment/{appointmentId}")
    public GlobalResult<List<MedicalRecord>> listByAppointment(@PathVariable("appointmentId") Integer appointmentId) {
        return GlobalResult.success(medicalRecordService.listByAppointment(appointmentId));
    }
}
