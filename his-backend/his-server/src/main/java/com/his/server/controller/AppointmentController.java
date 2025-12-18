package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.AppointmentDTO;
import com.his.server.entity.Appointment;
import com.his.server.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "挂号管理")
@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "创建挂号单")
    @PostMapping
    public GlobalResult<Appointment> create(@RequestBody AppointmentDTO dto) {
        return GlobalResult.success(appointmentService.createAppointment(dto));
    }

    @Operation(summary = "创建挂号单(当前登录患者)")
    @PostMapping("/me")
    public GlobalResult<Appointment> createForMe(@RequestBody AppointmentDTO dto, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Integer pid = session == null ? null : (Integer) session.getAttribute("pid");
        System.out.println("AppointmentController: session=" + (session != null ? session.getId() : "null") + ", pid=" + pid);
        return GlobalResult.success(appointmentService.createAppointmentBySession(pid, dto));
    }

    @Operation(summary = "查询患者的挂号记录")
    @GetMapping("/patient/{pid}")
    public GlobalResult<java.util.List<Appointment>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(appointmentService.listByPatient(pid));
    }

    @Operation(summary = "更新挂号状态")
    @PutMapping("/{id}/status")
    public GlobalResult<Appointment> updateStatus(@PathVariable("id") Integer id,
                                                  @RequestParam("status") Integer status) {
        return GlobalResult.success(appointmentService.updateStatus(id, status));
    }
}
