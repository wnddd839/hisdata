package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.entity.Appointment;
import com.his.server.entity.Doctor;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public List<Doctor> list(String department) {
        if (department != null && !department.isEmpty()) {
            return doctorRepository.findByDepartment(department);
        }
        return doctorRepository.findAll();
    }

    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor getById(Integer id) {
        return doctorRepository.findById(id).orElse(null);
    }

    /**
     * 获取医生详情(含统计信息)
     */
    public Map<String, Object> getDoctorDetail(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));

        Map<String, Object> detail = new HashMap<>();
        detail.put("doctorId", doctor.getDoctorId());
        detail.put("name", doctor.getName());
        detail.put("title", doctor.getTitle());
        detail.put("department", doctor.getDepartment());
        detail.put("phone", doctor.getPhone());

        return detail;
    }

    /**
     * 获取医生统计信息
     */
    public Map<String, Object> getDoctorStatistics(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));

        // 今日挂号数
        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentRepository.findByDoctorIdAndRegistrationDate(doctorId, today);

        // 待诊数
        long waitingCount = todayAppointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 1)
                .count();

        // 就诊中
        long inProgressCount = todayAppointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 2)
                .count();

        // 已完成
        long completedCount = todayAppointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 3)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("doctorId", doctorId);
        stats.put("doctorName", doctor.getName());
        stats.put("todayDate", today.toString());
        stats.put("todayTotal", todayAppointments.size());
        stats.put("waitingCount", waitingCount);
        stats.put("inProgressCount", inProgressCount);
        stats.put("completedCount", completedCount);

        return stats;
    }
}