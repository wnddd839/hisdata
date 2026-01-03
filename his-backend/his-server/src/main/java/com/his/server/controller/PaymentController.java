package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.common.exception.BusinessException;
import com.his.server.dto.PaymentCallbackDTO;
import com.his.server.entity.Appointment;
import com.his.server.entity.Finance;
import com.his.server.entity.Prescription;
import com.his.server.entity.Test;
import com.his.server.repository.AppointmentRepository;
import com.his.server.service.FinanceService;
import com.his.server.service.PatientService;
import com.his.server.service.PaymentService;
import com.his.server.service.PrescriptionService;
import com.his.server.service.TestService;
import com.his.server.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Tag(name = "支付管理")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final FinanceService financeService;
    private final PaymentService paymentService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final TestService testService;
    private final AppointmentRepository appointmentRepository;

    // ==================== 患者端查询接口 ====================

    @Operation(summary = "查询我的待支付汇总（挂号+检查+处方）")
    @GetMapping("/my-unpaid-bills")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> getMyUnpaidBills() {
        Integer userId = SecurityUtils.getCurrentUserId();

        // 1. 获取当前患者信息
        var patient = patientService.getCurrentPatient(userId);
        Integer pid = patient.getPid();

        // 2. 查询未支付的挂号单
        List<Appointment> unpaidAppointments = appointmentRepository
                .findByPidAndPaymentStatus(pid, "未支付");

        // 3. 查询未支付的检查单
        List<Test> unpaidTests = testService.listByPidAndStatus(pid, 0);

        // 4. 查询未支付的处方
        List<Prescription> unpaidPrescriptions = prescriptionService.listByPidAndStatus(pid, 0);

        // 5. 计算总金额
        BigDecimal appointmentFee = unpaidAppointments.stream()
                .map(Appointment::getRegistrationFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal testFee = unpaidTests.stream()
                .map(Test::getTestFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal prescriptionFee = unpaidPrescriptions.stream()
                .map(Prescription::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFee = appointmentFee.add(testFee).add(prescriptionFee);

        // 6. 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("pid", pid);
        result.put("patientName", patient.getName());

        // 挂号费信息
        result.put("unpaidAppointments", unpaidAppointments);
        result.put("appointmentCount", unpaidAppointments.size());
        result.put("appointmentFee", appointmentFee);

        // 检查费信息
        result.put("unpaidTests", unpaidTests);
        result.put("testCount", unpaidTests.size());
        result.put("testFee", testFee);

        // 处方费信息
        result.put("unpaidPrescriptions", unpaidPrescriptions);
        result.put("prescriptionCount", unpaidPrescriptions.size());
        result.put("prescriptionFee", prescriptionFee);

        // 总计
        result.put("totalCount", unpaidAppointments.size() + unpaidTests.size() + unpaidPrescriptions.size());
        result.put("totalFee", totalFee);

        return GlobalResult.success(result);
    }

    // ==================== 患者端支付接口 ====================

    @Operation(summary = "支付挂号费")
    @PostMapping("/pay-appointment")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> payAppointment(@RequestParam("appointmentId") Integer appointmentId) {
        log.info("患者支付挂号费: appointmentId={}", appointmentId);
        return GlobalResult.success(paymentService.payAppointment(appointmentId));
    }

    @Operation(summary = "支付检查费")
    @PostMapping("/pay-test")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> payTest(@RequestParam("testId") Integer testId) {
        log.info("患者支付检查费: testId={}", testId);
        return GlobalResult.success(paymentService.payTest(testId));
    }

    @Operation(summary = "批量支付检查费")
    @PostMapping("/pay-test-batch")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> payTestBatch(@RequestParam("testIds") String testIds) {
        log.info("患者批量支付检查费: testIds={}", testIds);
        return GlobalResult.success(paymentService.payTestBatch(testIds));
    }

    @Operation(summary = "支付处方药品费")
    @PostMapping("/pay-prescription")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> payPrescription(@RequestParam("prescriptionId") Integer prescriptionId) {
        log.info("患者支付处方药品费: prescriptionId={}", prescriptionId);
        return GlobalResult.success(paymentService.payPrescription(prescriptionId));
    }

    @Operation(summary = "批量支付处方药品费")
    @PostMapping("/pay-prescription-batch")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Map<String, Object>> payPrescriptionBatch(@RequestParam("prescriptionIds") String prescriptionIds) {
        log.info("患者批量支付处方药品费: prescriptionIds={}", prescriptionIds);
        return GlobalResult.success(paymentService.payPrescriptionBatch(prescriptionIds));
    }

    // ==================== 支付回调接口 ====================

    // 签名密钥,实际应该放在配置文件中
    private static final String SIGNATURE_KEY = "his_payment_signature_key_2024";

    @Operation(summary = "支付回调")
    @PostMapping("/callback")
    public GlobalResult<String> callback(@RequestBody PaymentCallbackDTO dto) {
        log.info("收到支付回调: financeId={}, status={}, transactionId={}",
                dto.getFinanceId(), dto.getStatus(), dto.getTransactionId());

        try {
            // 1. 验证签名
            if (!verifySignature(dto)) {
                log.warn("支付回调签名验证失败: {}", dto);
                return GlobalResult.error(403, "签名验证失败");
            }

            // 2. 验证时间戳(防止重放攻击,5分钟内有效)
            if (dto.getTimestamp() == null) {
                return GlobalResult.error(400, "缺少时间戳");
            }
            long callbackTime = dto.getTimestamp();
            long currentTime = Instant.now().getEpochSecond();
            if (Math.abs(currentTime - callbackTime) > 300) { // 5分钟 = 300秒
                log.warn("支付回调时间戳过期: callbackTime={}, currentTime={}", callbackTime, currentTime);
                return GlobalResult.error(400, "请求已过期");
            }

            // 3. 获取财务记录
            Finance finance = financeService.getById(dto.getFinanceId());
            if (finance == null) {
                log.warn("财务记录不存在: financeId={}", dto.getFinanceId());
                return GlobalResult.error(404, "财务记录不存在");
            }

            // 4. 验证金额
            if (dto.getAmount() != null && dto.getAmount().compareTo(finance.getTotalFee()) != 0) {
                log.warn("支付回调金额不匹配: expected={}, actual={}",
                        finance.getTotalFee(), dto.getAmount());
                return GlobalResult.error(400, "金额不匹配");
            }

            // 5. 幂等性处理
            if ("已支付".equals(finance.getPaymentStatus())) {
                log.info("重复回调,财务记录已支付: financeId={}", dto.getFinanceId());
                return GlobalResult.success("重复回调");
            }

            // 6. 处理支付
            if ("SUCCESS".equalsIgnoreCase(dto.getStatus())) {
                financeService.pay(dto.getFinanceId());
                log.info("支付回调处理成功: financeId={}, transactionId={}",
                        dto.getFinanceId(), dto.getTransactionId());
                return GlobalResult.success("回调处理成功");
            } else {
                log.info("支付状态非SUCCESS,忽略: status={}", dto.getStatus());
                return GlobalResult.success("回调忽略 (状态非SUCCESS)");
            }

        } catch (Exception e) {
            log.error("支付回调处理失败", e);
            return GlobalResult.error(500, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 验证签名
     * 签名算法: MD5(financeId + amount + status + timestamp + SECRET_KEY)
     */
    private boolean verifySignature(PaymentCallbackDTO dto) {
        if (dto.getSignature() == null || dto.getSignature().isEmpty()) {
            return false;
        }

        try {
            String expectedSignature = generateSignature(dto);
            return expectedSignature.equals(dto.getSignature());
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 生成签名
     */
    private String generateSignature(PaymentCallbackDTO dto) throws Exception {
        String data = String.format("%d%s%s%d%s",
                dto.getFinanceId(),
                dto.getAmount() != null ? dto.getAmount().toString() : "",
                dto.getStatus() != null ? dto.getStatus() : "",
                dto.getTimestamp() != null ? dto.getTimestamp() : 0,
                SIGNATURE_KEY);

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
