package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.PharmacyInventory;
import com.his.server.entity.Prescription;
import com.his.server.repository.PrescriptionRepository;
import com.his.server.service.PharmacyInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "药房管理")
@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyInventoryController {

    private final PharmacyInventoryService inventoryService;
    private final PrescriptionRepository prescriptionRepository;

    @Operation(summary = "查询药品库存")
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<List<PharmacyInventory>> list(@RequestParam(value = "name", required = false) String name,
                                                      @RequestParam(value = "category", required = false) String category) {
        return GlobalResult.success(inventoryService.list(name, category));
    }

    @Operation(summary = "查询临期药品(30天内)")
    @GetMapping("/inventory/expiring")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<List<PharmacyInventory>> listExpiring() {
        return GlobalResult.success(inventoryService.listExpiringSoon());
    }

    @Operation(summary = "添加/更新药品")
    @PostMapping("/inventory")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<PharmacyInventory> save(@RequestBody PharmacyInventory inventory) {
        return GlobalResult.success(inventoryService.save(inventory));
    }

    @Operation(summary = "待发药处方列表")
    @GetMapping("/prescriptions/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<List<Prescription>> listPendingPrescriptions() {
        return GlobalResult.success(prescriptionRepository.findByStatus(1));
    }

    @Operation(summary = "库存预检查")
    @PostMapping("/check-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<Boolean> checkStock(@RequestBody CheckStockDTO dto) {
        return GlobalResult.success(inventoryService.checkStock(dto.getMedicineId(), dto.getQuantity()));
    }

    @Operation(summary = "发药")
    @PostMapping("/dispense")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<String> dispense(@RequestBody DispenseDTO dto) {
        String operator = dto.getOperator() == null || dto.getOperator().isBlank() ? "admin" : dto.getOperator();
        inventoryService.dispenseMedicine(dto.getPrescriptionId(), dto.getMedicineId(), dto.getQuantity(), operator);
        return GlobalResult.success("发药成功");
    }

    @lombok.Data
    public static class DispenseDTO {
        private Integer prescriptionId;
        private Integer medicineId;
        private Integer quantity;
        private String operator;
    }

    @lombok.Data
    public static class CheckStockDTO {
        private Integer medicineId;
        private Integer quantity;
    }
}
