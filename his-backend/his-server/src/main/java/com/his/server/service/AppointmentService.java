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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public List<Appointment> listByPatient(Integer pid) {
        try {
            return appointmentRepository.findByPidOrPatientId(pid, pid);
        } catch (Exception e) {
            throw new BusinessException(500, "查询挂号记录失败: " + e.getMessage());
        }
    }

    @Transactional
    public Appointment createAppointment(AppointmentDTO dto) {
        // 1. 校验患者是否存在
        Patient patient = patientRepository.findById(dto.getPid())
                .orElseThrow(() -> new BusinessException("患者不存在"));

        // 2. 校验医生是否存在
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));

        // 3. 创建挂号单
        Appointment appointment = new Appointment();
        appointment.setPid(patient.getPid());
        appointment.setPatientId(patient.getPid());
        appointment.setDoctorId(doctor.getDoctorId());
        appointment.setDepartment(dto.getDepartment());
        appointment.setRegistrationDate(dto.getRegistrationDate());
        appointment.setRegistrationTime(LocalTime.now());
        appointment.setRegistrationFee(dto.getRegistrationFee());
        appointment.setStatus(1); // 1=待就诊

        // 4. 保存
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment createAppointmentBySession(Integer pid, AppointmentDTO dto) {
        if (pid == null) {
            throw new BusinessException(401, "未登录");
        }

        Patient patient = patientRepository.findById(pid)
            .orElseThrow(() -> new BusinessException("患者不存在"));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
            .orElseThrow(() -> new BusinessException("医生不存在"));

        Appointment appointment = new Appointment();
        appointment.setPid(patient.getPid());
        appointment.setPatientId(patient.getPid());
        appointment.setDoctorId(doctor.getDoctorId());
        appointment.setDepartment(dto.getDepartment());
        appointment.setRegistrationDate(dto.getRegistrationDate());
        appointment.setRegistrationTime(LocalTime.now());
        appointment.setRegistrationFee(dto.getRegistrationFee());
        appointment.setStatus(1);
        return appointmentRepository.save(appointment);
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
        
        return appointmentRepository.save(appointment);
    }
}
