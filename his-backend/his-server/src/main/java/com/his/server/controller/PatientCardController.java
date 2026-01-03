package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Patient;
import com.his.server.service.PatientService;
import com.his.server.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "就诊卡管理")
@RestController
@RequestMapping("/api/patient-card")
@RequiredArgsConstructor
public class PatientCardController {

    private final PatientService patientService;

    @Operation(summary = "查询我的所有就诊卡")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Patient>> getMyCards() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return GlobalResult.success(patientService.listByUserId(userId));
    }

    @Operation(summary = "查询我的所有就诊卡（前端专用接口）")
    @GetMapping("/my-cards")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<List<Patient>> getMyCardsAlias() {
        return getMyCards();
    }

    @Operation(summary = "创建就诊卡")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Patient> createCard(@RequestBody CreatePatientCardDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return GlobalResult.success(patientService.createPatientCard(userId, dto));
    }

    @Operation(summary = "更新就诊卡信息")
    @PutMapping("/{cardId}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Patient> updateCard(
            @PathVariable Integer cardId,
            @RequestBody CreatePatientCardDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        return GlobalResult.success(patientService.updatePatientCard(userId, cardId, dto));
    }

    @Operation(summary = "删除就诊卡")
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Void> deleteCard(@PathVariable Integer cardId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        patientService.deletePatientCard(userId, cardId);
        return GlobalResult.success();
    }

    @Operation(summary = "查询就诊卡详情（通过ID）")
    @GetMapping("/id/{cardId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<Patient> getCardById(@PathVariable Integer cardId) {
        return GlobalResult.success(patientService.getPatientCard(cardId));
    }

    @Operation(summary = "通过就诊卡号查询（标准路径）")
    @GetMapping("/number/{cardNumber}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<Patient> getByCardNumber(@PathVariable String cardNumber) {
        Patient patient = patientService.getByCardNumber(cardNumber);
        if (patient == null) {
            return GlobalResult.error(404, "就诊卡不存在");
        }
        return GlobalResult.success(patient);
    }

    // 保留向后兼容
    @Operation(summary = "查询就诊卡详情（旧版兼容）")
    @GetMapping("/{cardId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN')")
    public GlobalResult<Patient> getCardLegacy(@PathVariable Integer cardId) {
        return getCardById(cardId);
    }

    @Operation(summary = "补办就诊卡")
    @PostMapping("/reissue")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public GlobalResult<Patient> reissueCard() {
        Integer userId = SecurityUtils.getCurrentUserId();
        Patient newCard = patientService.reissueCard(userId);
        return GlobalResult.success(newCard);
    }

    @Data
    public static class CreatePatientCardDTO {
        private String name;
        private String gender;
        private Integer age;
        private String idCard;
        private String phone;
        private String address;
        private String allergy;
    }
}
