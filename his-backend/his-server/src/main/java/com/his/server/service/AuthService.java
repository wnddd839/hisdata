package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.dto.AuthLoginDTO;
import com.his.server.dto.AuthRegisterDTO;
import com.his.server.dto.AuthUserVO;
import com.his.server.entity.Patient;
import com.his.server.entity.UserAccount;
import com.his.server.repository.PatientRepository;
import com.his.server.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthUserVO register(AuthRegisterDTO dto) {
        String phone = dto.getPhone() == null ? null : dto.getPhone().trim();
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度至少6位");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (dto.getGender() == null || (dto.getGender() != 1 && dto.getGender() != 2)) {
            throw new BusinessException(400, "性别必须为1或2");
        }
        if (dto.getAge() == null || dto.getAge() < 0 || dto.getAge() > 150) {
            throw new BusinessException(400, "年龄不合法");
        }

        if (userAccountRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException(409, "手机号已注册");
        }

        Patient patient = new Patient();
        patient.setName(dto.getName().trim());
        patient.setGender(dto.getGender());
        patient.setAge(dto.getAge());
        patient.setPhone(phone);
        patient.setAddress(dto.getAddress());
        patient.setAllergy(dto.getAllergy());
        Patient savedPatient = patientRepository.save(patient);

        UserAccount user = new UserAccount();
        user.setPid(savedPatient.getPid());
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        UserAccount savedUser = userAccountRepository.save(user);

        AuthUserVO vo = new AuthUserVO();
        vo.setUserId(savedUser.getUserId());
        vo.setPid(savedPatient.getPid());
        vo.setPhone(savedUser.getPhone());
        vo.setName(savedPatient.getName());
        return vo;
    }

    public AuthUserVO login(AuthLoginDTO dto) {
        String phone = dto.getPhone() == null ? null : dto.getPhone().trim();
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new BusinessException(400, "密码不能为空");
        }

        UserAccount user = userAccountRepository.findByPhone(phone)
            .orElseThrow(() -> new BusinessException(401, "手机号或密码错误"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "手机号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号不可用");
        }

        Patient patient = patientRepository.findById(user.getPid())
            .orElseThrow(() -> new BusinessException(404, "患者档案不存在"));

        AuthUserVO vo = new AuthUserVO();
        vo.setUserId(user.getUserId());
        vo.setPid(patient.getPid());
        vo.setPhone(user.getPhone());
        vo.setName(patient.getName());
        return vo;
    }
}

