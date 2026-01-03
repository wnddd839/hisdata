package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.controller.PatientCardController.CreatePatientCardDTO;
import com.his.server.dto.PatientDTO;
import com.his.server.entity.Patient;
import com.his.server.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> searchByName(String name) {
        return patientRepository.findByNameContaining(name);
    }

    public Patient getByCardNumber(String cardNumber) {
        return patientRepository.findByCardNumber(cardNumber).orElse(null);
    }

    public List<Patient> getMyPatients(Integer userId) {
        return patientRepository.findByUserId(userId);
    }

    /**
     * 查询用户的所有就诊卡
     */
    public List<Patient> listByUserId(Integer userId) {
        return patientRepository.findByUserId(userId);
    }

    /**
     * 创建就诊卡
     */
    @Transactional
    public Patient createPatientCard(Integer userId, CreatePatientCardDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }

        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setName(dto.getName().trim());
        // Convert String gender to Integer (1=男, 2=女)
        if (dto.getGender() != null) {
            patient.setGender("男".equals(dto.getGender()) || "1".equals(dto.getGender()) ? 1 : 2);
        }
        patient.setAge(dto.getAge());
        patient.setIdCard(dto.getIdCard());
        patient.setPhone(dto.getPhone());
        patient.setAddress(dto.getAddress());
        patient.setAllergy(dto.getAllergy());

        // 生成就诊卡号: P + yyyyMMddHHmmss + 3位随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(900) + 100;
        patient.setCardNumber("P" + timestamp + random);

        return patientRepository.save(patient);
    }

    /**
     * 更新就诊卡信息
     */
    @Transactional
    public Patient updatePatientCard(Integer userId, Integer cardId, CreatePatientCardDTO dto) {
        Patient patient = patientRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(404, "就诊卡不存在"));

        // 验证所有权
        if (!patient.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此就诊卡");
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            patient.setName(dto.getName().trim());
        }
        if (dto.getGender() != null) {
            patient.setGender("男".equals(dto.getGender()) || "1".equals(dto.getGender()) ? 1 : 2);
        }
        if (dto.getAge() != null) {
            patient.setAge(dto.getAge());
        }
        if (dto.getPhone() != null) {
            patient.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            patient.setAddress(dto.getAddress());
        }
        if (dto.getAllergy() != null) {
            patient.setAllergy(dto.getAllergy());
        }

        return patientRepository.save(patient);
    }

    /**
     * 删除就诊卡
     */
    @Transactional
    public void deletePatientCard(Integer userId, Integer cardId) {
        Patient patient = patientRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(404, "就诊卡不存在"));

        // 验证所有权
        if (!patient.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此就诊卡");
        }

        patientRepository.delete(patient);
    }

    /**
     * 获取就诊卡详情
     */
    public Patient getPatientCard(Integer cardId) {
        return patientRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(404, "就诊卡不存在"));
    }

    /**
     * 补办就诊卡（生成新的就诊卡号）
     */
    @Transactional
    public Patient reissueCard(Integer userId) {
        List<Patient> patients = patientRepository.findByUserId(userId);
        if (patients.isEmpty()) {
            throw new BusinessException(404, "用户档案不存在");
        }

        Patient patient = patients.get(0);

        // 生成就诊卡号: P + yyyyMMddHHmmss + 3位随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(900) + 100;
        patient.setCardNumber("P" + timestamp + random);

        return patientRepository.save(patient);
    }

    public Patient getCurrentPatient(Integer userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        List<Patient> patients = patientRepository.findByUserId(userId);
        if (patients.isEmpty()) {
            throw new BusinessException(404, "用户档案不存在");
        }
        return patients.get(0);
    }

    public Patient getOwnedPatientByCardNumber(Integer userId, String cardNumber) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new BusinessException(400, "就诊卡号不能为空");
        }
        Patient patient = patientRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new BusinessException(404, "就诊卡号不存在"));
        if (patient.getUserId() == null || !patient.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问该就诊人");
        }
        return patient;
    }

    @Transactional
    public Patient createPatient(PatientDTO dto, Integer userId) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (dto.getIdCard() == null || dto.getIdCard().length() != 18) {
            throw new BusinessException(400, "身份证号格式错误");
        }

        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setName(dto.getName());
        patient.setGender(dto.getGender());
        patient.setAge(dto.getAge());
        patient.setIdCard(dto.getIdCard());
        patient.setPhone(dto.getPhone());
        patient.setAddress(dto.getAddress());
        patient.setAllergy(dto.getAllergy());
        patient.setMedicalHistory(dto.getMedicalHistory());
        
        // Generate Card Number: P + yyyyMMddHHmmss + 3 random digits
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(900) + 100;
        patient.setCardNumber("P" + timestamp + random);

        return patientRepository.save(patient);
    }

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    public Patient getById(Integer id) {
        return patientRepository.findById(id).orElse(null);
    }
}
