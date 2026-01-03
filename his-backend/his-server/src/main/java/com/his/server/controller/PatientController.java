package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Patient;
import com.his.server.service.PatientService;
import com.his.server.dto.PatientDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "患者管理")
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @Operation(summary = "搜索患者(按姓名)")
    @GetMapping("/search")
    public GlobalResult<List<Patient>> search(@RequestParam("name") String name) {
        return GlobalResult.success(patientService.searchByName(name));
    }

    @Operation(summary = "按病历号搜索患者")
    @GetMapping("/search-by-card")
    public GlobalResult<Patient> searchByCardNumber(@RequestParam("cardNumber") String cardNumber) {
        Patient patient = patientService.getByCardNumber(cardNumber);
        if (patient == null) {
            return GlobalResult.error(404, "未找到该病历号对应的患者");
        }
        return GlobalResult.success(patient);
    }

    @Operation(summary = "获取当前用户的就诊人信息")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Patient> getMyPatient(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return GlobalResult.error(401, "未登录");
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_UID_KEY);
        if (userId == null) return GlobalResult.error(401, "未登录");

        try {
            return GlobalResult.success(patientService.getCurrentPatient(userId));
        } catch (Exception e) {
            return GlobalResult.success(null);
        }
    }


    @Operation(summary = "创建/更新患者 (管理员)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR')")
    public GlobalResult<Patient> save(@RequestBody Patient patient) {
        return GlobalResult.success(patientService.save(patient));
    }
    
    @Operation(summary = "获取患者详情")
    @GetMapping("/{id}")
    public GlobalResult<Patient> getById(@PathVariable("id") Integer id) {
        return GlobalResult.success(patientService.getById(id));
    }
}
