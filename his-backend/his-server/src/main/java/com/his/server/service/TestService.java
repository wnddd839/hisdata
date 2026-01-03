package com.his.server.service;

import com.his.server.dto.TestDTO;
import com.his.common.exception.BusinessException;
import com.his.server.entity.Appointment;
import com.his.server.entity.Test;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.TestRepository;
import com.his.server.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;

    public List<Test> listByPatient(Integer pid) {
        return testRepository.findByPid(pid);
    }

    public List<Test> listByAppointment(Integer appointmentId) {
        return testRepository.findByAppointmentId(appointmentId);
    }

    public List<Test> listPending() {
        return testRepository.findByStatus(1); // 1=已支付/待检查
    }

    public List<Test> listPendingByDoctor(Integer doctorId) {
        return testRepository.findByDoctorIdAndStatus(doctorId, 1); // 1=已支付/待检查
    }

    public List<Test> listByDoctor(Integer doctorId) {
        return testRepository.findByDoctorId(doctorId); // 查询医生所有检查（不限制状态）
    }

    @Transactional
    public Test createTest(TestDTO dto) {
        if (dto.getAppointmentId() == null) {
            throw new BusinessException(400, "appointmentId不能为空");
        }
        if (dto.getDoctorId() == null) {
            throw new BusinessException(400, "doctorId不能为空");
        }
        if (dto.getPid() == null) {
            throw new BusinessException(400, "pid不能为空");
        }
        if (dto.getTestType() == null) {
            throw new BusinessException(400, "testType不能为空");
        }
        if (dto.getTestFee() == null || dto.getTestFee().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "testFee无效");
        }

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new BusinessException(404, "挂号单不存在"));
        if (!dto.getDoctorId().equals(appointment.getDoctorId())) {
            throw new BusinessException(403, "无权为该挂号单开检查");
        }
        if (!dto.getPid().equals(appointment.getPid())) {
            throw new BusinessException(400, "pid与挂号单不匹配");
        }
        if (appointment.getStatus() == null || appointment.getStatus() != 2) {
            throw new BusinessException(400, "仅就诊中挂号单允许开检查");
        }

        Test test = new Test();
        test.setPid(dto.getPid());
        test.setDoctorId(dto.getDoctorId());
        test.setAppointmentId(dto.getAppointmentId());
        test.setTestType(dto.getTestType());
        test.setTestFee(dto.getTestFee());
        test.setTestDate(LocalDate.now());
        test.setStatus(0); // 0=未支付
        
        return testRepository.save(test);
    }

    /**
     * 查询当前患者的未支付检查
     */
    public Map<String, Object> listMyUnpaid() {
        Integer userId = SecurityUtils.getCurrentUserId();
        // 获取当前登录患者的就诊卡
        var patient = patientService.getCurrentPatient(userId);

        // 查询该患者所有未支付的检查(status=0)
        List<Test> unpaidTests = testRepository.findByPidAndStatus(patient.getPid(), 0);

        Map<String, Object> result = new HashMap<>();
        result.put("pid", patient.getPid());
        result.put("patientName", patient.getName());
        result.put("unpaidTests", unpaidTests);
        result.put("count", unpaidTests.size());

        // 计算总金额
        var totalAmount = unpaidTests.stream()
                .map(Test::getTestFee)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        result.put("totalAmount", totalAmount);

        return result;
    }

    @Transactional
    public Test updateStatus(Integer testId, Integer status, String result) {
        Test test = testRepository.findById(testId).orElseThrow(() -> new RuntimeException("检查不存在"));
        test.setStatus(status);
        if (result != null) {
            test.setResult(result);
        }
        return testRepository.save(test);
    }
}
