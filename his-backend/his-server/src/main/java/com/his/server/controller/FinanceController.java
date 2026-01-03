package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Appointment;
import com.his.server.entity.Finance;
import com.his.server.entity.Patient;
import com.his.server.repository.AppointmentRepository;
import com.his.server.service.FinanceService;
import com.his.server.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "财务管理")
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;
    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;

    @Operation(summary = "生成账单")
    @PostMapping("/bill/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<Finance> generateBill(@PathVariable("appointmentId") Integer appointmentId,
                                              HttpServletRequest request) {
        checkPatientOwnershipIfNeeded(appointmentId, request);
        String discountCode = request.getParameter("discountCode");
        return GlobalResult.success(financeService.generateBill(appointmentId, discountCode));
    }

    @Operation(summary = "支付")
    @PostMapping("/{financeId}/pay")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<Finance> pay(@PathVariable("financeId") Integer financeId, HttpServletRequest request) {
        Finance existing = financeService.getById(financeId);
        if (existing != null) {
            checkPatientOwnershipIfNeeded(existing.getAppointmentId(), request);
        }
        return GlobalResult.success(financeService.pay(financeId));
    }

    @Operation(summary = "支付账单（前端文档路径）")
    @PostMapping("/pay/{financeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_ADMIN')")
    public GlobalResult<Finance> payAlias(@PathVariable("financeId") Integer financeId, HttpServletRequest request) {
        return pay(financeId, request);
    }

    @Operation(summary = "查询挂号单账单")
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<List<Finance>> listByAppointment(@PathVariable("appointmentId") Integer appointmentId, HttpServletRequest request) {
        checkPatientOwnershipIfNeeded(appointmentId, request);
        return GlobalResult.success(financeService.listByAppointment(appointmentId));
    }

    @Operation(summary = "查询当前患者的所有账单")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Finance>> listMyBills(HttpServletRequest request) {
        // 获取当前登录用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.his.common.exception.BusinessException(401, "未登录");
        }

        Integer userId = null;
        if (authentication.getPrincipal() instanceof Integer) {
            userId = (Integer) authentication.getPrincipal();
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                userId = (Integer) session.getAttribute(AuthController.SESSION_UID_KEY);
            }
        }

        if (userId == null) {
            throw new com.his.common.exception.BusinessException(401, "未登录");
        }

        // 获取患者信息
        Patient patient = patientService.getCurrentPatient(userId);
        if (patient == null) {
            throw new com.his.common.exception.BusinessException(404, "患者信息不存在");
        }

        // 查询患者的所有挂号
        List<Appointment> appointments = appointmentRepository.findByPid(patient.getPid());

        // 查询所有挂号的账单
        List<Finance> allBills = new java.util.ArrayList<>();
        for (Appointment apt : appointments) {
            List<Finance> bills = financeService.listByAppointment(apt.getAppointmentId());
            allBills.addAll(bills);
        }

        return GlobalResult.success(allBills);
    }

    @Operation(summary = "查询我的账单（前端专用接口）")
    @GetMapping("/my-bills")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Finance>> listMyBillsAlias(HttpServletRequest request) {
        return listMyBills(request);
    }

    @Operation(summary = "查询待缴费账单")
    @GetMapping("/my-unpaid")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Finance>> listMyUnpaidBills(HttpServletRequest request) {
        // 获取当前登录用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.his.common.exception.BusinessException(401, "未登录");
        }

        Integer userId = null;
        if (authentication.getPrincipal() instanceof Integer) {
            userId = (Integer) authentication.getPrincipal();
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                userId = (Integer) session.getAttribute(AuthController.SESSION_UID_KEY);
            }
        }

        if (userId == null) {
            throw new com.his.common.exception.BusinessException(401, "未登录");
        }

        // 获取患者信息
        Patient patient = patientService.getCurrentPatient(userId);
        if (patient == null) {
            throw new com.his.common.exception.BusinessException(404, "患者信息不存在");
        }

        // 查询待缴费账单
        List<Finance> unpaidBills = financeService.listByPidAndStatus(patient.getPid(), "未支付");
        return GlobalResult.success(unpaidBills);
    }

    private void checkPatientOwnershipIfNeeded(Integer appointmentId, HttpServletRequest request) {
        // 从SecurityContext获取认证信息(支持JWT和Session)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.his.common.exception.BusinessException(401, "未登录");
        }

        // 获取用户ID和角色
        Integer userId = null;
        String role = null;

        // 从Authentication中获取(支持JWT)
        if (authentication.getPrincipal() instanceof Integer) {
            userId = (Integer) authentication.getPrincipal();
            role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse(null);
        }

        // Fallback: 从Session获取
        if (userId == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                userId = (Integer) session.getAttribute(AuthController.SESSION_UID_KEY);
                role = (String) session.getAttribute(AuthController.SESSION_ROLE_KEY);
            }
        }

        // 非患者角色不需要验证所有权
        if (!"ROLE_PATIENT".equals(role)) {
            return;
        }

        // 患者角色必须验证所有权
        if (userId == null) {
            throw new com.his.common.exception.BusinessException(401, "未登录");
        }

        Patient patient = patientService.getCurrentPatient(userId);
        if (patient == null) {
            throw new com.his.common.exception.BusinessException(404, "患者信息不存在");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new com.his.common.exception.BusinessException(404, "挂号单不存在"));

        if (!patient.getPid().equals(appointment.getPid())) {
            throw new com.his.common.exception.BusinessException(403, "无权访问该挂号单账单");
        }
    }
}
