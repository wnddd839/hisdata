package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.dto.AppointmentDTO;
import com.his.server.entity.Appointment;
import com.his.server.service.AppointmentService;
import com.his.server.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "挂号管理")
@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "创建挂号单 (指定就诊卡)")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Appointment> create(@RequestBody AppointmentDTO dto) {
        return GlobalResult.success(appointmentService.createAppointment(dto));
    }

    @Operation(summary = "创建挂号单（前端专用路径）")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Appointment> createWithPath(@RequestBody AppointmentDTO dto) {
        return create(dto);
    }

    @Operation(summary = "创建挂号单(当前登录患者) - Deprecated: Use /api/appointment")
    @PostMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Appointment> createForMe(@RequestBody AppointmentDTO dto) {
        return create(dto);
    }

    @Operation(summary = "查询患者的挂号记录")
    @GetMapping("/patient/{pid}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_ADMIN', 'ROLE_DOCTOR')")
    public GlobalResult<List<Appointment>> listByPatient(@PathVariable("pid") Integer pid) {
        return GlobalResult.success(appointmentService.listByPatient(pid));
    }

    @Operation(summary = "查询当前登录用户的挂号记录")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Appointment>> listMyAppointments() {
        return GlobalResult.success(appointmentService.listByUserId(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "查询我的挂号记录（前端专用接口）")
    @GetMapping("/my-appointments")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Appointment>> listMyAppointmentsAlias() {
        return listMyAppointments();
    }

    @Operation(summary = "取消挂号")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<Void> cancel(@PathVariable("id") Integer id) {
        appointmentService.cancelAppointment(id);
        return GlobalResult.success(null);
    }

    @Operation(summary = "查询医生的所有挂号记录")
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<List<Appointment>> listByDoctor(@PathVariable("doctorId") Integer doctorId) {
        return GlobalResult.success(appointmentService.listByDoctor(doctorId));
    }

    @Operation(summary = "更新挂号状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<Appointment> updateStatus(@PathVariable("id") Integer id,
                                                  @RequestParam("status") Integer status) {
        return GlobalResult.success(appointmentService.updateStatus(id, status));
    }

    @Operation(summary = "获取候诊队列(医生端)")
    @GetMapping("/queue")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<java.util.List<Appointment>> getQueue(@RequestParam("doctorId") Integer doctorId) {
        return GlobalResult.success(appointmentService.listWaitingQueue(doctorId));
    }

    @Operation(summary = "获取候诊队列(医生端)- 前端文档路径")
    @GetMapping("/queue/{doctorId}")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<java.util.List<Appointment>> getQueueByPath(@PathVariable("doctorId") Integer doctorId) {
        return GlobalResult.success(appointmentService.listWaitingQueue(doctorId));
    }

    @Operation(summary = "叫号(医生端)")
    @PostMapping("/{id}/call")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Void> callPatient(@PathVariable("id") Integer id) {
        appointmentService.callPatient(id);
        return GlobalResult.success(null);
    }

    @Operation(summary = "今日挂号列表(管理员/医生)")
    @GetMapping("/today")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR')")
    public GlobalResult<List<Appointment>> listTodayAppointments() {
        return GlobalResult.success(appointmentService.listTodayAppointments());
    }
}
