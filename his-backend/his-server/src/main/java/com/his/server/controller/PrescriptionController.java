package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.PrescriptionDTO;
import com.his.server.entity.Prescription;
import com.his.server.service.PatientService;
import com.his.server.service.PrescriptionService;
import com.his.server.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "处方管理")
@RestController
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PatientService patientService;

    @Operation(summary = "开具处方")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Prescription> create(@RequestBody PrescriptionDTO dto) {
        return GlobalResult.success(prescriptionService.createPrescription(dto));
    }

    @Operation(summary = "开具处方（前端专用路径）")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Prescription> createWithPath(@RequestBody PrescriptionDTO dto) {
        return create(dto);
    }

    @Operation(summary = "批量开具处方")
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<java.util.List<Prescription>> createBatch(@RequestBody java.util.List<PrescriptionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return GlobalResult.error(400, "处方列表不能为空");
        }
        return GlobalResult.success(prescriptionService.createPrescriptionsBatch(dtos));
    }

    @Operation(summary = "查询患者处方")
    @GetMapping("/patient/{pid}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<List<Prescription>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(prescriptionService.listByPatient(pid));
    }

    @Operation(summary = "查询挂号单处方")
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<List<Prescription>> listByAppointment(@PathVariable("appointmentId") Integer appointmentId) {
        return GlobalResult.success(prescriptionService.listByAppointment(appointmentId));
    }

    @Operation(summary = "库存预检查")
    @PostMapping("/check-stock")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Boolean> checkStock(@RequestBody CheckStockDTO dto) {
        // 如果库存不足，checkStock方法会抛出异常，全局异常处理器会捕获并返回错误信息
        // 这里为了符合 check-stock 语义，也可以捕获异常返回 false，或者让前端处理错误
        // 简单起见，我们直接调用 checkStock，成功则返回 true
        prescriptionService.checkStock(dto.getMedicineId(), dto.getQuantity());
        return GlobalResult.success(true);
    }

    @Operation(summary = "查询我的未支付处方")
    @GetMapping("/my-unpaid")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> listMyUnpaid() {
        return GlobalResult.success(prescriptionService.listMyUnpaid());
    }

    @Operation(summary = "查询我的处方")
    @GetMapping("/my-prescriptions")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Prescription>> listMyPrescriptions() {
        var patient = patientService.getCurrentPatient(SecurityUtils.getCurrentUserId());
        return GlobalResult.success(prescriptionService.listByPatient(patient.getPid()));
    }

    @Operation(summary = "更新处方状态")
    @PutMapping("/{prescriptionId}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_PHARMACIST', 'ROLE_ADMIN')")
    public GlobalResult<Prescription> updateStatus(
            @PathVariable("prescriptionId") Integer prescriptionId,
            @RequestParam("status") Integer status) {
        return GlobalResult.success(prescriptionService.updateStatus(prescriptionId, status));
    }

    @lombok.Data
    public static class CheckStockDTO {
        private Integer medicineId;
        private Integer quantity;
    }
}
