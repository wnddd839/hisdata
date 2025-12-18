package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public GlobalResult<Map<String, Object>> getDailyStats() {
        return GlobalResult.success(statisticsService.getDailyStats());
    }

    @Operation(summary = "营收趋势(近7天)")
    @GetMapping("/revenue")
    public GlobalResult<Map<String, Object>> getRevenueStats() {
        return GlobalResult.success(statisticsService.getRevenueStats());
    }

    @Operation(summary = "热门医生Top5")
    @GetMapping("/top-doctors")
    public GlobalResult<List<Map<String, Object>>> getTopDoctors() {
        return GlobalResult.success(statisticsService.getTopDoctors());
    }
}
