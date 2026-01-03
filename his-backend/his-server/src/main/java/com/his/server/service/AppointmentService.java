package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.dto.AppointmentDTO;
import com.his.server.entity.Appointment;
import com.his.server.entity.Doctor;
import com.his.server.entity.Patient;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.DoctorRepository;
import com.his.server.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<Appointment> listByPatient(Integer pid) {
        try {
            return appointmentRepository.findByPidOrPatientId(pid, pid);
        } catch (Exception e) {
            throw new BusinessException(500, "查询挂号记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户的所有挂号记录（通过用户ID）
     */
    public List<Appointment> listByUserId(Integer userId) {
        try {
            return appointmentRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new BusinessException(500, "查询挂号记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询医生指定日期的挂号记录
     */
    public List<Appointment> listByDoctorAndDate(Integer doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndRegistrationDate(doctorId, date);
    }

    /**
     * 查询医生的所有挂号记录（不限制日期）
     */
    public List<Appointment> listByDoctor(Integer doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    /**
     * 获取医生的候诊队列（待诊和就诊中）
     */
    public List<Appointment> getWaitingQueue(Integer doctorId) {
        LocalDate today = LocalDate.now();
        List<Appointment> queue = appointmentRepository.findByDoctorIdAndRegistrationDate(doctorId, today);
        return queue.stream()
                .filter(a -> a.getStatus() != null && (a.getStatus() == 1 || a.getStatus() == 2))
                .sorted((a, b) -> {
                    // 按状态排序：待诊 > 就诊中
                    if (a.getStatus().equals(b.getStatus())) {
                        return a.getSerialNumber().compareTo(b.getSerialNumber());
                    }
                    return a.getStatus().compareTo(b.getStatus());
                })
                .toList();
    }

    public List<Appointment> listWaitingQueue(Integer doctorId) {
        return appointmentRepository.findByDoctorIdAndRegistrationDateAndStatusOrderBySerialNumberAsc(
            doctorId, java.time.LocalDate.now(), 1 // 1=待就诊
        );
    }

    /**
     * 统计指定日期的挂号数量
     */
    public long countByDate(LocalDate date) {
        return appointmentRepository.countByRegistrationDate(date);
    }

    /**
     * 统计指定日期和状态的挂号数量
     */
    public long countByDateAndStatus(LocalDate date, Integer status) {
        return appointmentRepository.countByRegistrationDateAndStatus(date, status);
    }

    /**
     * 查询今日所有挂号列表
     */
    public List<Appointment> listTodayAppointments() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByRegistrationDateOrderByRegistrationTimeDesc(today);
    }

    public void callPatient(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("挂号单不存在"));
        
        // 广播叫号消息
        // 格式: { type: "CALL", appointmentId: 1, name: "张三", serialNumber: 5, dept: "内科" }
        java.util.Map<String, Object> message = new java.util.HashMap<>();
        message.put("type", "CALL");
        message.put("appointmentId", appointment.getAppointmentId());
        message.put("name", patientRepository.findById(appointment.getPid()).map(Patient::getName).orElse("未知"));
        message.put("serialNumber", appointment.getSerialNumber());
        message.put("department", appointment.getDepartment());
        message.put("doctorId", appointment.getDoctorId());
        
        messagingTemplate.convertAndSend("/topic/call", message);
    }

    @Transactional
    public void cancelAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("挂号单不存在"));

        if (appointment.getStatus() == 3) {
            throw new BusinessException("已完成的挂号单无法取消");
        }

        appointment.setStatus(0); // 0=已取消
        appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment createAppointment(AppointmentDTO dto) {
        try {
            return doCreateAppointment(dto);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("号源已被抢占，请重试");
        }
    }

    public Appointment createAppointmentBySession(Integer pid, AppointmentDTO dto) {
         if (pid != null) {
            dto.setPid(pid);
         }
         return createAppointment(dto);
    }

    private Appointment doCreateAppointment(AppointmentDTO dto) {
        // 获取就诊卡信息
        Patient patient;
        if (dto.getPid() != null) {
            patient = patientRepository.findById(dto.getPid())
                    .orElseThrow(() -> new BusinessException(404, "就诊卡不存在"));

            // 如果传了userId，验证所有权
            if (dto.getUserId() != null && !patient.getUserId().equals(dto.getUserId())) {
                throw new BusinessException(403, "无权使用此就诊卡");
            }
        } else if (dto.getCardNumber() != null && !dto.getCardNumber().isEmpty()) {
            patient = patientRepository.findByCardNumber(dto.getCardNumber())
                    .orElseThrow(() -> new BusinessException(404, "就诊卡号无效: " + dto.getCardNumber()));

            // 验证所有权
            if (dto.getUserId() != null && !patient.getUserId().equals(dto.getUserId())) {
                throw new BusinessException(403, "无权使用此就诊卡");
            }

            dto.setPid(patient.getPid());
        } else {
            throw new BusinessException(400, "请选择就诊卡");
        }

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));

        LocalDate registrationDate = dto.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }

        // 所有医生默认任何时刻都可挂号,无需验证排班

        Appointment appointment = new Appointment();
        appointment.setPid(patient.getPid());
        appointment.setPatientId(patient.getPid());
        appointment.setPatientName(patient.getName());
        appointment.setDoctorId(doctor.getDoctorId());
        appointment.setDoctorName(doctor.getName());
        appointment.setDepartment(dto.getDepartment());
        appointment.setScheduleId(null); // 不再使用排班系统
        appointment.setRegistrationDate(registrationDate);
        LocalTime time = dto.getRegistrationTime();
        if (time == null) {
            time = LocalTime.now();
        }
        appointment.setRegistrationTime(time);
        BigDecimal registrationFee = dto.getRegistrationFee();
        if (registrationFee == null) {
            registrationFee = new BigDecimal("10.00");
        }
        if (registrationFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "registrationFee无效");
        }
        appointment.setRegistrationFee(registrationFee);
        appointment.setStatus(1);

        int serialNumber = appointmentRepository
                .findByDoctorIdAndRegistrationDate(doctor.getDoctorId(), registrationDate)
                .size() + 1;
        appointment.setSerialNumber(serialNumber);

        // 5. 保存并广播
        Appointment savedAppointment = appointmentRepository.save(appointment);
        messagingTemplate.convertAndSend("/topic/appointments", savedAppointment);

        return savedAppointment;
    }

    @Transactional
    public Appointment updateStatus(Integer appointmentId, Integer status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("挂号单不存在"));
        
        appointment.setStatus(status);
        
        // 如果是就诊中(2)，记录开始时间
        if (status == 2) {
            appointment.setConsultStartTime(java.time.LocalDateTime.now());
        } 
        // 如果是已完成(3)，记录结束时间
        else if (status == 3) {
            appointment.setConsultEndTime(java.time.LocalDateTime.now());
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        messagingTemplate.convertAndSend("/topic/appointments", savedAppointment);
        return savedAppointment;
    }
}
