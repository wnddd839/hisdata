package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "今日经营概况")
    @GetMapping("/daily")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<Map<String, Object>> getDailyStats() {
        return GlobalResult.success(statisticsService.getDailyStats());
    }

    @Operation(summary = "营收趋势(近7天)")
    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<Map<String, Object>> getRevenueStats() {
        return GlobalResult.success(statisticsService.getRevenueStats());
    }

    @Operation(summary = "热门医生Top5")
    @GetMapping("/top-doctors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<List<Map<String, Object>>> getTopDoctors() {
        return GlobalResult.success(statisticsService.getTopDoctors());
    }

    @Operation(summary = "挂号统计(兼容接口)")
    @GetMapping("/registration")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<Map<String, Object>> getRegistrationStats() {
        return GlobalResult.success(statisticsService.getDailyStats());
    }

    @Operation(summary = "医生工作量(兼容接口)")
    @GetMapping("/doctor/workload")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GlobalResult<List<Map<String, Object>>> getDoctorWorkload() {
        return GlobalResult.success(statisticsService.getTopDoctors());
    }
}
