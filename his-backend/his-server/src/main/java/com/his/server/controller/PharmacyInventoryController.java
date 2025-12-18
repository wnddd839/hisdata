package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.PharmacyInventory;
import com.his.server.service.PharmacyInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "药房管理")
@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyInventoryController {

    private final PharmacyInventoryService inventoryService;

    @Operation(summary = "查询药品库存")
    @GetMapping("/inventory")
    public GlobalResult<List<PharmacyInventory>> list(@RequestParam(value = "name", required = false) String name,
                                                      @RequestParam(value = "category", required = false) String category) {
        return GlobalResult.success(inventoryService.list(name, category));
    }

    @Operation(summary = "查询临期药品(30天内)")
    @GetMapping("/inventory/expiring")
    public GlobalResult<List<PharmacyInventory>> listExpiring() {
        return GlobalResult.success(inventoryService.listExpiringSoon());
    }

    @Operation(summary = "添加/更新药品")
    @PostMapping("/inventory")
    public GlobalResult<PharmacyInventory> save(@RequestBody PharmacyInventory inventory) {
        return GlobalResult.success(inventoryService.save(inventory));
    }

    @Operation(summary = "发药")
    @PostMapping("/dispense")
    public GlobalResult<String> dispense(@RequestParam("prescriptionId") Integer prescriptionId,
                                         @RequestParam("medicineId") Integer medicineId,
                                         @RequestParam("quantity") Integer quantity,
                                         @RequestParam("operator") String operator) {
        inventoryService.dispenseMedicine(prescriptionId, medicineId, quantity, operator);
        return GlobalResult.success("发药成功");
    }
}
