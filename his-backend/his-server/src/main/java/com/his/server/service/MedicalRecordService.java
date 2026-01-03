package com.his.server.service;

import com.his.server.dto.MedicalRecordDTO;
import com.his.common.exception.BusinessException;
import com.his.server.entity.Appointment;
import com.his.server.entity.MedicalRecord;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public List<MedicalRecord> listByPatient(Integer pid) {
        return medicalRecordRepository.findByPid(pid);
    }

    public List<MedicalRecord> listByAppointment(Integer appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId);
    }

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecordDTO dto) {
        if (dto.getAppointmentId() == null) {
            throw new BusinessException(400, "appointmentId不能为空");
        }
        if (dto.getDoctorId() == null) {
            throw new BusinessException(400, "doctorId不能为空");
        }
        if (dto.getPid() == null) {
            throw new BusinessException(400, "pid不能为空");
        }
        if (dto.getChiefComplaint() == null || dto.getChiefComplaint().isBlank()) {
            throw new BusinessException(400, "主诉不能为空");
        }
        if (dto.getPresentIllness() == null || dto.getPresentIllness().isBlank()) {
            throw new BusinessException(400, "现病史不能为空");
        }
        if (dto.getPhysicalExamination() == null || dto.getPhysicalExamination().isBlank()) {
            throw new BusinessException(400, "体格检查不能为空");
        }
        if (dto.getPreliminaryDiagnosis() == null || dto.getPreliminaryDiagnosis().isBlank()) {
            throw new BusinessException(400, "初步诊断不能为空");
        }

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new BusinessException(404, "挂号单不存在"));
        if (!dto.getDoctorId().equals(appointment.getDoctorId())) {
            throw new BusinessException(403, "无权为该挂号单写病历");
        }
        if (!dto.getPid().equals(appointment.getPid())) {
            throw new BusinessException(400, "pid与挂号单不匹配");
        }
        if (appointment.getStatus() == null || appointment.getStatus() != 2) {
            throw new BusinessException(400, "仅就诊中挂号单允许写病历");
        }

        MedicalRecord record = new MedicalRecord();
        record.setPid(dto.getPid());
        record.setDoctorId(dto.getDoctorId());
        record.setAppointmentId(dto.getAppointmentId());
        record.setChiefComplaint(dto.getChiefComplaint());
        record.setPresentIllness(dto.getPresentIllness());
        record.setPhysicalExamination(dto.getPhysicalExamination());
        record.setPreliminaryDiagnosis(dto.getPreliminaryDiagnosis());
        
        return medicalRecordRepository.save(record);
    }

    @Transactional
    public MedicalRecord updateMedicalRecord(Integer recordId, MedicalRecordDTO dto) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(404, "病历不存在"));

        // 验证基本字段
        if (dto.getChiefComplaint() != null && !dto.getChiefComplaint().isBlank()) {
            record.setChiefComplaint(dto.getChiefComplaint());
        }
        if (dto.getPresentIllness() != null && !dto.getPresentIllness().isBlank()) {
            record.setPresentIllness(dto.getPresentIllness());
        }
        if (dto.getPhysicalExamination() != null && !dto.getPhysicalExamination().isBlank()) {
            record.setPhysicalExamination(dto.getPhysicalExamination());
        }
        if (dto.getPreliminaryDiagnosis() != null && !dto.getPreliminaryDiagnosis().isBlank()) {
            record.setPreliminaryDiagnosis(dto.getPreliminaryDiagnosis());
        }
        if (dto.getTreatmentPlan() != null) {
            record.setTreatmentPlan(dto.getTreatmentPlan());
        }
        if (dto.getNotes() != null) {
            record.setNotes(dto.getNotes());
        }

        return medicalRecordRepository.save(record);
    }

    @Transactional
    public void deleteMedicalRecord(Integer recordId) {
        if (!medicalRecordRepository.existsById(recordId)) {
            throw new BusinessException(404, "病历不存在");
        }
        medicalRecordRepository.deleteById(recordId);
    }
}
