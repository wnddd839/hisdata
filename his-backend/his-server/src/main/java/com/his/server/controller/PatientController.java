package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Patient;
import com.his.server.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "患者管理")
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @Operation(summary = "搜索患者(按姓名)")
    @GetMapping("/search")
    public GlobalResult<List<Patient>> search(@RequestParam("name") String name) {
        return GlobalResult.success(patientService.searchByName(name));
    }

    @Operation(summary = "创建/更新患者")
    @PostMapping
    public GlobalResult<Patient> save(@RequestBody Patient patient) {
        return GlobalResult.success(patientService.save(patient));
    }
    
    @Operation(summary = "获取患者详情")
    @GetMapping("/{id}")
    public GlobalResult<Patient> getById(@PathVariable("id") Integer id) {
        return GlobalResult.success(patientService.getById(id));
    }
}
