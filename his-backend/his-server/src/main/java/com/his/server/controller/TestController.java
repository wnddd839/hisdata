package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.TestDTO;
import com.his.server.entity.Test;
import com.his.server.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "检查管理")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @Operation(summary = "申请检查")
    @PostMapping
    public GlobalResult<Test> create(@RequestBody TestDTO dto) {
        return GlobalResult.success(testService.createTest(dto));
    }

    @Operation(summary = "查询患者检查")
    @GetMapping("/patient/{pid}")
    public GlobalResult<List<Test>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(testService.listByPatient(pid));
    }

    @Operation(summary = "查询挂号单检查")
    @GetMapping("/appointment/{appointmentId}")
    public GlobalResult<List<Test>> listByAppointment(@PathVariable("appointmentId") Integer appointmentId) {
        return GlobalResult.success(testService.listByAppointment(appointmentId));
    }

    @Operation(summary = "更新检查状态/结果")
    @PutMapping("/{testId}/status")
    public GlobalResult<Test> updateStatus(@PathVariable("testId") Integer testId,
                                           @RequestParam("status") Integer status,
                                           @RequestParam(value = "result", required = false) String result) {
        return GlobalResult.success(testService.updateStatus(testId, status, result));
    }
}
