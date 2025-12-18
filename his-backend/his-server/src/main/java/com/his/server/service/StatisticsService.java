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
        stats.put("revenue", todayRevenue);
        stats.put("appointments", todayAppointments);
        return stats;
    }

    public Map<String, Object> getRevenueStats() {
        // Last 7 days
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            
            BigDecimal revenue = financeRepository.sumTotalFeeByPaymentStatusAndPaymentTimeBetween("已支付", start, end);
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date);
            dayStat.put("revenue", revenue);
            result.add(dayStat);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("chartData", result);
        return response;
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
