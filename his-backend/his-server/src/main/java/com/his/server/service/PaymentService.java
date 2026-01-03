package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.entity.Appointment;
import com.his.server.entity.Test;
import com.his.server.entity.Prescription;
import com.his.server.repository.AppointmentRepository;
import com.his.server.repository.TestRepository;
import com.his.server.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付服务
 * 处理挂号费、检查费和处方药品费的支付逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final AppointmentRepository appointmentRepository;
    private final TestRepository testRepository;
    private final PrescriptionRepository prescriptionRepository;

    /**
     * 支付挂号费
     *
     * @param appointmentId 挂号单ID
     * @return 支付结果信息
     */
    @Transactional
    public Map<String, Object> payAppointment(Integer appointmentId) {
        // 1. 查询挂号单
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(404, "挂号单不存在"));

        // 2. 检查挂号单状态
        if (appointment.getStatus() == null || appointment.getStatus() == 0) {
            throw new BusinessException(400, "该挂号单已取消，无法支付");
        }

        // 3. 检查是否已支付
        if ("已支付".equals(appointment.getPaymentStatus())) {
            throw new BusinessException(400, "该挂号单已支付，请勿重复支付");
        }

        // 4. 更新支付状态
        appointment.setPaymentStatus("已支付");
        appointment.setPaymentTime(LocalDateTime.now());
        appointmentRepository.save(appointment);

        // 5. 模拟支付成功（测试环境不需要真实支付）
        log.info("挂号费支付成功: appointmentId={}, fee={}",
                appointmentId, appointment.getRegistrationFee());

        // 6. 返回支付结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "挂号费支付成功");
        result.put("appointmentId", appointment.getAppointmentId());
        result.put("fee", appointment.getRegistrationFee());
        result.put("paymentStatus", "已支付");
        result.put("paymentTime", appointment.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("transactionId", "APT_" + System.currentTimeMillis()); // 模拟交易流水号

        return result;
    }

    /**
     * 支付检查费
     *
     * @param testId 检查单ID
     * @return 支付结果信息
     */
    @Transactional
    public Map<String, Object> payTest(Integer testId) {
        // 1. 查询检查单
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(404, "检查单不存在"));

        // 2. 检查是否已支付
        if (test.getStatus() == null || test.getStatus() != 0) {
            throw new BusinessException(400, "该检查单已支付或状态异常");
        }

        // 3. 更新检查单状态为已支付/待检查
        test.setStatus(1); // 1=已支付/待检查
        testRepository.save(test);

        // 4. 模拟支付成功
        log.info("检查费支付成功: testId={}, fee={}", testId, test.getTestFee());

        // 5. 返回支付结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "检查费支付成功");
        result.put("testId", test.getTestId());
        result.put("fee", test.getTestFee());
        result.put("paymentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("transactionId", "TEST_" + System.currentTimeMillis()); // 模拟交易流水号

        return result;
    }

    /**
     * 批量支付检查费
     *
     * @param testIds 检查单ID列表，格式: "1,2,3"
     * @return 支付结果信息
     */
    @Transactional
    public Map<String, Object> payTestBatch(String testIds) {
        // 1. 解析ID列表
        String[] idArray = testIds.split(",");
        int successCount = 0;
        int failCount = 0;
        double totalFee = 0;
        StringBuilder transactionIds = new StringBuilder();
        List<Map<String, Object>> successItems = new java.util.ArrayList<>();
        List<Map<String, Object>> failItems = new java.util.ArrayList<>();

        // 2. 循环支付每个检查单
        for (String idStr : idArray) {
            try {
                Integer testId = Integer.parseInt(idStr.trim());
                Map<String, Object> result = payTest(testId);

                if ((Boolean) result.get("success")) {
                    successCount++;
                    totalFee += ((Number) result.get("fee")).doubleValue();
                    transactionIds.append(result.get("transactionId")).append(",");

                    // 记录成功的项目
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("testId", testId);
                    item.put("fee", result.get("fee"));
                    item.put("transactionId", result.get("transactionId"));
                    successItems.add(item);
                }

            } catch (Exception e) {
                log.warn("批量支付失败: testId={}, error={}", idStr, e.getMessage());
                failCount++;

                // 记录失败的项目
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("testId", idStr.trim());
                item.put("error", e.getMessage());
                failItems.add(item);
            }
        }

        // 3. 返回批量支付结果（提供详细的成功和失败列表）
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", String.format("批量支付完成：成功%d笔，失败%d笔", successCount, failCount));
        result.put("totalCount", idArray.length);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalFee", totalFee);
        result.put("paymentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("transactionIds", transactionIds.toString());
        result.put("successItems", successItems);  // 成功的项目列表
        result.put("failItems", failItems);        // 失败的项目列表

        // 如果全部失败，返回整体失败
        if (successCount == 0) {
            result.put("success", false);
            result.put("message", "批量支付全部失败");
        }

        return result;
    }

    /**
     * 支付处方药品费
     *
     * @param prescriptionId 处方ID
     * @return 支付结果信息
     */
    @Transactional
    public Map<String, Object> payPrescription(Integer prescriptionId) {
        // 1. 查询处方
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new BusinessException(404, "处方不存在"));

        // 2. 检查是否已支付
        if (prescription.getStatus() == null || prescription.getStatus() != 0) {
            throw new BusinessException(400, "该处方已支付或状态异常");
        }

        // 3. 更新处方状态为已支付/待发药
        prescription.setStatus(1); // 1=已支付/待发药
        prescriptionRepository.save(prescription);

        // 4. 模拟支付成功
        log.info("处方药品费支付成功: prescriptionId={}, medicineName={}, fee={}",
                prescriptionId, prescription.getMedicineName(), prescription.getTotalCost());

        // 5. 返回支付结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "处方药品费支付成功");
        result.put("prescriptionId", prescription.getPrescriptionId());
        result.put("medicineName", prescription.getMedicineName());
        result.put("fee", prescription.getTotalCost());
        result.put("paymentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("transactionId", "RX_" + System.currentTimeMillis()); // 模拟交易流水号

        return result;
    }

    /**
     * 批量支付处方药品费
     *
     * @param prescriptionIds 处方ID列表，格式: "1,2,3"
     * @return 支付结果信息
     */
    @Transactional
    public Map<String, Object> payPrescriptionBatch(String prescriptionIds) {
        // 1. 解析ID列表
        String[] idArray = prescriptionIds.split(",");
        int successCount = 0;
        int failCount = 0;
        double totalFee = 0;
        StringBuilder transactionIds = new StringBuilder();
        List<Map<String, Object>> successItems = new java.util.ArrayList<>();
        List<Map<String, Object>> failItems = new java.util.ArrayList<>();

        // 2. 循环支付每个处方
        for (String idStr : idArray) {
            try {
                Integer prescriptionId = Integer.parseInt(idStr.trim());
                Map<String, Object> result = payPrescription(prescriptionId);

                if ((Boolean) result.get("success")) {
                    successCount++;
                    totalFee += ((Number) result.get("fee")).doubleValue();
                    transactionIds.append(result.get("transactionId")).append(",");

                    // 记录成功的项目
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("prescriptionId", prescriptionId);
                    item.put("medicineName", result.get("medicineName"));
                    item.put("fee", result.get("fee"));
                    item.put("transactionId", result.get("transactionId"));
                    successItems.add(item);
                }

            } catch (Exception e) {
                log.warn("批量支付失败: prescriptionId={}, error={}", idStr, e.getMessage());
                failCount++;

                // 记录失败的项目
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("prescriptionId", idStr.trim());
                item.put("error", e.getMessage());
                failItems.add(item);
            }
        }

        // 3. 返回批量支付结果（提供详细的成功和失败列表）
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", String.format("批量处方支付完成：成功%d笔，失败%d笔", successCount, failCount));
        result.put("totalCount", idArray.length);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalFee", totalFee);
        result.put("paymentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("transactionIds", transactionIds.toString());
        result.put("successItems", successItems);  // 成功的项目列表
        result.put("failItems", failItems);        // 失败的项目列表

        // 如果全部失败，返回整体失败
        if (successCount == 0) {
            result.put("success", false);
            result.put("message", "批量处方支付全部失败");
        }

        return result;
    }
}
