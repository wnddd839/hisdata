package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.dto.PrescriptionDTO;
import com.his.server.entity.Appointment;
import com.his.server.entity.PharmacyInventory;
import com.his.server.entity.Prescription;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.PrescriptionRepository;
import com.his.server.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PharmacyInventoryService inventoryService;
    private final PatientService patientService;

    public List<Prescription> listByPatient(Integer pid) {
        return prescriptionRepository.findByPid(pid);
    }

    public List<Prescription> listByAppointment(Integer appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }

    public void checkStock(Integer medicineId, Integer quantity) {
        inventoryService.checkStock(medicineId, quantity);
    }

    @Transactional
    public Prescription createPrescription(PrescriptionDTO dto) {
        if (dto.getAppointmentId() == null) {
            throw new BusinessException(400, "appointmentId不能为空");
        }
        if (dto.getDoctorId() == null) {
            throw new BusinessException(400, "doctorId不能为空");
        }
        if (dto.getPid() == null) {
            throw new BusinessException(400, "pid不能为空");
        }
        if (dto.getMedicineId() == null) {
            throw new BusinessException(400, "medicineId不能为空");
        }
        if (dto.getDosage() == null || dto.getDosage().isBlank()) {
            throw new BusinessException(400, "dosage不能为空");
        }
        if (dto.getDosageUnit() == null || dto.getDosageUnit().isBlank()) {
            throw new BusinessException(400, "dosageUnit不能为空");
        }
        if (dto.getFrequency() == null || dto.getFrequency().isBlank()) {
            throw new BusinessException(400, "frequency不能为空");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException(400, "quantity无效");
        }

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new BusinessException(404, "挂号单不存在"));
        if (!dto.getDoctorId().equals(appointment.getDoctorId())) {
            throw new BusinessException(403, "无权为该挂号单开处方");
        }
        if (!dto.getPid().equals(appointment.getPid())) {
            throw new BusinessException(400, "pid与挂号单不匹配");
        }
        if (appointment.getStatus() == null || appointment.getStatus() != 2) {
            throw new BusinessException(400, "仅就诊中挂号单允许开处方");
        }

        // 1. 检查库存
        inventoryService.checkStock(dto.getMedicineId(), dto.getQuantity());

        // 2. 获取药品信息计算价格
        PharmacyInventory medicine = inventoryService.getById(dto.getMedicineId());
        if (medicine == null) {
            throw new BusinessException("药品不存在");
        }

        // 3. 创建处方
        Prescription prescription = new Prescription();
        prescription.setPid(dto.getPid());
        prescription.setDoctorId(dto.getDoctorId());
        prescription.setAppointmentId(dto.getAppointmentId());
        prescription.setMedicineId(dto.getMedicineId());
        prescription.setMedicineName(medicine.getName());
        prescription.setDosage(dto.getDosage());
        prescription.setDosageUnit(dto.getDosageUnit());
        prescription.setFrequency(dto.getFrequency());
        prescription.setQuantity(dto.getQuantity());

        // 计算总价
        BigDecimal totalCost = medicine.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        prescription.setTotalCost(totalCost);

        return prescriptionRepository.save(prescription);
    }

    /**
     * 批量创建处方
     */
    @Transactional
    public List<Prescription> createPrescriptionsBatch(List<PrescriptionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BusinessException(400, "处方列表不能为空");
        }

        // 验证所有处方的appointmentId、doctorId、pid一致
        if (dtos.size() > 1) {
            PrescriptionDTO first = dtos.get(0);
            for (int i = 1; i < dtos.size(); i++) {
                PrescriptionDTO current = dtos.get(i);
                if (!first.getAppointmentId().equals(current.getAppointmentId())) {
                    throw new BusinessException(400, "批量处方必须属于同一挂号单");
                }
                if (!first.getDoctorId().equals(current.getDoctorId())) {
                    throw new BusinessException(400, "批量处方必须来自同一医生");
                }
                if (!first.getPid().equals(current.getPid())) {
                    throw new BusinessException(400, "批量处方必须属于同一患者");
                }
            }
        }

        // 批量创建处方
        List<Prescription> prescriptions = new java.util.ArrayList<>();
        for (PrescriptionDTO dto : dtos) {
            prescriptions.add(createPrescription(dto));
        }

        return prescriptions;
    }

    /**
     * 查询当前患者的未支付处方
     */
    public Map<String, Object> listMyUnpaid() {
        Integer userId = SecurityUtils.getCurrentUserId();
        // 获取当前登录患者的就诊卡
        var patient = patientService.getCurrentPatient(userId);

        // 查询该患者所有未支付的处方(status=0)
        List<Prescription> unpaidPrescriptions = prescriptionRepository.findByPidAndStatus(patient.getPid(), 0);

        Map<String, Object> result = new HashMap<>();
        result.put("pid", patient.getPid());
        result.put("patientName", patient.getName());
        result.put("unpaidPrescriptions", unpaidPrescriptions);
        result.put("count", unpaidPrescriptions.size());

        // 计算总金额
        var totalAmount = unpaidPrescriptions.stream()
                .map(Prescription::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalAmount", totalAmount);

        return result;
    }

    /**
     * 更新处方状态
     */
    @Transactional
    public Prescription updateStatus(Integer prescriptionId, Integer status) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new BusinessException(404, "处方不存在"));

        prescription.setStatus(status);
        return prescriptionRepository.save(prescription);
    }
}
