package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Appointment;
import com.his.server.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "临床管理")
@RestController
@RequestMapping("/api/clinical")
@RequiredArgsConstructor
public class ClinicalController {

    private final AppointmentService appointmentService;

    @Operation(summary = "获取医生候诊队列")
    @GetMapping("/my-queue")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<List<Appointment>> getMyQueue(@RequestParam Integer doctorId) {
        LocalDate today = LocalDate.now();
        List<Appointment> queue = appointmentService.listByDoctorAndDate(doctorId, today);

        // 按就诊序号排序，过滤掉已取消的
        List<Appointment> result = queue.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() != 0)
                .sorted((a, b) -> a.getSerialNumber().compareTo(b.getSerialNumber()))
                .toList();

        return GlobalResult.success(result);
    }
}
