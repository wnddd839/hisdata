package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.service.AppointmentService;
import com.his.server.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "管理员仪表板")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardController {

    private final StatisticsService statisticsService;
    private final AppointmentService appointmentService;

    @Operation(summary = "系统概览")
    @GetMapping("/overview")
    public GlobalResult<Map<String, Object>> getOverview() {
        return GlobalResult.success(statisticsService.getSystemOverview());
    }

    @Operation(summary = "今日统计")
    @GetMapping("/today")
    public GlobalResult<Map<String, Object>> getTodayStats() {
        return GlobalResult.success(statisticsService.getTodayStats());
    }

    @Operation(summary = "营收统计")
    @GetMapping("/revenue")
    public GlobalResult<Map<String, Object>> getRevenueStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String department) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        return GlobalResult.success(statisticsService.getRevenueStats(start, end, department));
    }

    @Operation(summary = "科室统计")
    @GetMapping("/department")
    public GlobalResult<Map<String, Object>> getDepartmentStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        return GlobalResult.success(statisticsService.getDepartmentStats(start, end));
    }

    @Operation(summary = "医生工作量统计")
    @GetMapping("/doctors")
    public GlobalResult<Map<String, Object>> getDoctorStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        return GlobalResult.success(statisticsService.getDoctorStats(start, end, limit));
    }

    @Operation(summary = "热门科室排行")
    @GetMapping("/top-departments")
    public GlobalResult<Map<String, Object>> getTopDepartments(
            @RequestParam(required = false, defaultValue = "7") Integer days,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        LocalDate start = LocalDate.now().minusDays(days);
        LocalDate end = LocalDate.now();

        return GlobalResult.success(statisticsService.getTopDepartments(start, end, limit));
    }

    @Operation(summary = "实时挂号情况")
    @GetMapping("/realtime-appointments")
    public GlobalResult<Map<String, Object>> getRealtimeAppointments() {
        LocalDate today = LocalDate.now();
        long totalAppointments = appointmentService.countByDate(today);
        long waitingCount = appointmentService.countByDateAndStatus(today, 1);
        long inProgressCount = appointmentService.countByDateAndStatus(today, 2);
        long completedCount = appointmentService.countByDateAndStatus(today, 3);

        Map<String, Object> stats = Map.of(
                "date", today.toString(),
                "total", totalAppointments,
                "waiting", waitingCount,
                "inProgress", inProgressCount,
                "completed", completedCount
        );

        return GlobalResult.success(stats);
    }
}
