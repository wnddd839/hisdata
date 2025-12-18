package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.PrescriptionDTO;
import com.his.server.entity.Prescription;
import com.his.server.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "处方管理")
@RestController
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @Operation(summary = "开具处方")
    @PostMapping
    public GlobalResult<Prescription> create(@RequestBody PrescriptionDTO dto) {
        return GlobalResult.success(prescriptionService.createPrescription(dto));
    }

    @Operation(summary = "查询患者处方")
    @GetMapping("/patient/{pid}")
    public GlobalResult<List<Prescription>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(prescriptionService.listByPatient(pid));
    }
}
