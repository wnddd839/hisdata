package com.his.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.exception.BusinessException;
import com.his.server.entity.Appointment;
import com.his.server.entity.Discount;
import com.his.server.entity.Finance;
import com.his.server.entity.Prescription;
import com.his.server.entity.Test;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.FinanceRepository;
import com.his.server.repository.PrescriptionRepository;
import com.his.server.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceRepository financeRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final TestRepository testRepository;
    private final DiscountService discountService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Finance generateBill(Integer appointmentId, String discountCode) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("挂号单不存在"));

        List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
        List<Test> tests = testRepository.findByAppointmentId(appointmentId);

        BigDecimal regFee = appointment.getRegistrationFee();
        if (regFee == null) {
            regFee = BigDecimal.ZERO;
        }
        BigDecimal medicineFee = prescriptions.stream()
                .map(Prescription::getTotalCost)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal testFee = tests.stream()
                .map(Test::getTestFee)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFee = regFee.add(medicineFee).add(testFee);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discountCode != null && !discountCode.isEmpty()) {
            Discount discount = discountService.getValidByCode(discountCode);
            discountAmount = discount.getDiscountValue();
            if (discountAmount.compareTo(totalFee) > 0) {
                discountAmount = totalFee;
            }
        }

        BigDecimal finalTotal = totalFee.subtract(discountAmount);

        Finance finance = new Finance();
        finance.setPid(appointment.getPid()); // 设置就诊卡号关联
        finance.setAppointmentId(appointmentId);
        finance.setRegistrationFee(regFee);
        finance.setMedicineFee(medicineFee);
        finance.setTestFee(testFee);
        finance.setDiscount(discountAmount);
        finance.setTotalFee(finalTotal);
        finance.setPaymentStatus("未支付");
        
        // 构建 feeDetails JSON
        Map<String, BigDecimal> details = new HashMap<>();
        details.put("挂号费", regFee);
        details.put("药品费", medicineFee);
        details.put("检查费", testFee);
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            details.put("优惠", discountAmount.negate());
        }
        try {
            finance.setFeeDetails(objectMapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            throw new BusinessException("费用明细生成失败");
        }

        return financeRepository.save(finance);
    }

    @Transactional
    public Finance pay(Integer financeId) {
        Finance finance = financeRepository.findById(financeId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

        if ("已支付".equals(finance.getPaymentStatus())) {
            throw new BusinessException("账单已支付");
        }

        finance.setPaymentStatus("已支付");
        finance.setPaymentTime(LocalDateTime.now());

        // 只更新未支付的处方和检查单状态，避免状态冲突
        // 如果处方/检查已经单独支付过（status=1），则跳过
        List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(finance.getAppointmentId());
        for (Prescription p : prescriptions) {
            // 只更新状态为0（未支付）的处方
            if (p.getStatus() == null || p.getStatus() == 0) {
                p.setStatus(1); // 1=待发药
                prescriptionRepository.save(p);
            }
            // status=1（已单独支付）或status=2（已发药）的保持不变
        }

        // 只更新未支付的检查单状态
        List<Test> tests = testRepository.findByAppointmentId(finance.getAppointmentId());
        for (Test t : tests) {
            // 只更新状态为0（未支付）的检查单
            if (t.getStatus() == null || t.getStatus() == 0) {
                t.setStatus(1); // 1=待检查
                testRepository.save(t);
            }
            // status=1（已单独支付）或status=2（已完成）的保持不变
        }

        return financeRepository.save(finance);
    }

    public List<Finance> listByAppointment(Integer appointmentId) {
        return financeRepository.findByAppointmentId(appointmentId);
    }

    public List<Finance> listByPid(Integer pid) {
        return financeRepository.findByPid(pid);
    }

    public List<Finance> listByPidAndStatus(Integer pid, String paymentStatus) {
        return financeRepository.findByPidAndPaymentStatus(pid, paymentStatus);
    }

    public Finance getById(Integer financeId) {
        return financeRepository.findById(financeId).orElse(null);
    }
}
