package com.his.server.service;

import com.his.server.entity.Doctor;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.DoctorRepository;
import com.his.server.repository.FinanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final FinanceRepository financeRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public Map<String, Object> getDailyStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        BigDecimal todayRevenue = financeRepository.sumTotalFeeByPaymentStatusAndPaymentTimeBetween("已支付", startOfDay, endOfDay);
        long todayAppointments = appointmentRepository.countByRegistrationDate(today);

        Map<String, Object> stats = new HashMap<>();
        stats.put("date", today);
        stats.put("revenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        stats.put("appointments", todayAppointments);
        return stats;
    }

    /**
     * 系统概览
     */
    public Map<String, Object> getSystemOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        // 今日数据
        BigDecimal todayRevenue = financeRepository.sumTotalFeeByPaymentStatusAndPaymentTimeBetween("已支付", startOfDay, endOfDay);
        long todayAppointments = appointmentRepository.countByRegistrationDate(today);

        // 累计数据
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();

        Map<String, Object> overview = new HashMap<>();
        overview.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        overview.put("todayAppointments", todayAppointments);
        overview.put("totalDoctors", totalDoctors);
        overview.put("totalAppointments", totalAppointments);

        return overview;
    }

    /**
     * 今日统计
     */
    public Map<String, Object> getTodayStats() {
        return getDailyStats();
    }

    /**
     * 营收统计
     */
    public Map<String, Object> getRevenueStats(LocalDate startDate, LocalDate endDate, String department) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate current = startDate;

        BigDecimal totalRevenue = BigDecimal.ZERO;

        while (!current.isAfter(endDate)) {
            LocalDateTime start = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(current, LocalTime.MAX);

            BigDecimal revenue = financeRepository.sumTotalFeeByPaymentStatusAndPaymentTimeBetween("已支付", start, end);
            if (revenue != null) {
                totalRevenue = totalRevenue.add(revenue);
            }

            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", current.toString());
            dayStat.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
            result.add(dayStat);

            current = current.plusDays(1);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("chartData", result);
        response.put("totalRevenue", totalRevenue);
        response.put("startDate", startDate.toString());
        response.put("endDate", endDate.toString());

        return response;
    }

    /**
     * 科室统计
     */
    public Map<String, Object> getDepartmentStats(LocalDate startDate, LocalDate endDate) {
        // 按科室分组统计挂号量
        Map<String, Object> stats = new HashMap<>();
        stats.put("startDate", startDate.toString());
        stats.put("endDate", endDate.toString());
        stats.put("departments", new ArrayList<>()); // TODO: 实现科室统计逻辑

        return stats;
    }

    /**
     * 医生工作量统计
     */
    public Map<String, Object> getDoctorStats(LocalDate startDate, LocalDate endDate, Integer limit) {
        List<Object[]> results = appointmentRepository.findTopDoctors(PageRequest.of(0, limit));
        List<Map<String, Object>> doctorStats = new ArrayList<>();

        for (Object[] row : results) {
            Integer doctorId = (Integer) row[0];
            Long count = (Long) row[1];

            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("doctorId", doctor.getDoctorId());
                map.put("name", doctor.getName());
                map.put("department", doctor.getDepartment());
                map.put("appointmentCount", count);
                doctorStats.add(map);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorStats);
        response.put("startDate", startDate.toString());
        response.put("endDate", endDate.toString());

        return response;
    }

    /**
     * 热门科室排行
     */
    public Map<String, Object> getTopDepartments(LocalDate startDate, LocalDate endDate, Integer limit) {
        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate.toString());
        response.put("endDate", endDate.toString());
        response.put("departments", new ArrayList<>()); // TODO: 实现热门科室统计

        return response;
    }

    public Map<String, Object> getRevenueStats() {
        // Last 7 days
        LocalDate today = LocalDate.now();
        return getRevenueStats(today.minusDays(6), today, null);
    }

    public List<Map<String, Object>> getTopDoctors() {
        List<Object[]> results = appointmentRepository.findTopDoctors(PageRequest.of(0, 5));
        List<Map<String, Object>> topDoctors = new ArrayList<>();

        for (Object[] row : results) {
            Integer doctorId = (Integer) row[0];
            Long count = (Long) row[1];

            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("doctorId", doctor.getDoctorId());
                map.put("name", doctor.getName());
                map.put("department", doctor.getDepartment());
                map.put("appointmentCount", count);
                topDoctors.add(map);
            }
        }
        return topDoctors;
    }
}
