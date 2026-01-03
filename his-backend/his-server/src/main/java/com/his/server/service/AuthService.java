package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.dto.AuthLoginDTO;
import com.his.server.dto.AuthRegisterDTO;
import com.his.server.dto.AuthUserVO;
import com.his.server.dto.DoctorRegisterDTO;
import com.his.server.entity.Doctor;
import com.his.server.entity.LoginLog;
import com.his.server.entity.UserAccount;
import com.his.server.repository.DoctorRepository;
import com.his.server.repository.LoginLogRepository;
import com.his.server.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final DoctorRepository doctorRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientService patientService;
    private final com.his.server.dto.PatientDTO patientDTO = new com.his.server.dto.PatientDTO(); // helper for copy

    @Transactional
    public AuthUserVO register(AuthRegisterDTO dto) {
        String phone = dto.getPhone() == null ? null : dto.getPhone().trim();
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度至少6位");
        }

        if (userAccountRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException(409, "手机号已注册");
        }

        UserAccount user = new UserAccount();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_PATIENT");
        UserAccount savedUser = userAccountRepository.save(user);

        // Auto create patient profile
        com.his.server.dto.PatientDTO pDto = new com.his.server.dto.PatientDTO();
        pDto.setName(dto.getName() != null ? dto.getName() : "患者");
        pDto.setGender(dto.getGender() != null ? dto.getGender() : 1);
        pDto.setAge(dto.getAge() != null ? dto.getAge() : 0);
        pDto.setPhone(phone);
        pDto.setAddress(dto.getAddress());
        pDto.setAllergy(dto.getAllergy());
        pDto.setIdCard(dto.getIdCard());
        patientService.createPatient(pDto, savedUser.getUserId());

        AuthUserVO vo = new AuthUserVO();
        vo.setUserId(savedUser.getUserId());
        vo.setPhone(savedUser.getPhone());
        vo.setRole(savedUser.getRole());
        return vo;
    }

    @Transactional
    public AuthUserVO registerPatientSimple(String phone, String password) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException(400, "密码长度至少6位");
        }
        if (phone.length() != 11) {
            throw new BusinessException(400, "手机号格式不正确");
        }

        phone = phone.trim();
        if (userAccountRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException(409, "手机号已注册");
        }

        UserAccount user = new UserAccount();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("ROLE_PATIENT");
        user.setStatus(1);
        UserAccount savedUser = userAccountRepository.save(user);

        // Auto create patient profile with default values
        com.his.server.dto.PatientDTO pDto = new com.his.server.dto.PatientDTO();
        pDto.setName("患者");
        pDto.setGender(1);
        pDto.setAge(0);
        pDto.setPhone(phone);
        pDto.setAddress("");
        pDto.setAllergy("");
        pDto.setIdCard("");
        patientService.createPatient(pDto, savedUser.getUserId());

        AuthUserVO vo = new AuthUserVO();
        vo.setUserId(savedUser.getUserId());
        vo.setPhone(savedUser.getPhone());
        vo.setName("患者");
        vo.setRole(savedUser.getRole());
        return vo;
    }

    @Transactional
    public AuthUserVO registerDoctor(DoctorRegisterDTO dto) {
        String phone = dto.getPhone() == null ? null : dto.getPhone().trim();
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (userAccountRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException(409, "手机号已注册");
        }

        Doctor doctor = new Doctor();
        doctor.setName(dto.getName());
        doctor.setTitle(dto.getTitle());
        doctor.setDepartment(dto.getDepartment());
        doctor.setPhone(phone);
        Doctor savedDoctor = doctorRepository.save(doctor);

        UserAccount user = new UserAccount();
        user.setDoctorId(savedDoctor.getDoctorId());
        user.setPhone(phone);
        user.setRole("ROLE_DOCTOR");
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        UserAccount savedUser = userAccountRepository.save(user);

        AuthUserVO vo = new AuthUserVO();
        vo.setUserId(savedUser.getUserId());
        vo.setDoctorId(savedDoctor.getDoctorId());
        vo.setPhone(savedUser.getPhone());
        vo.setName(savedDoctor.getName());
        vo.setRole(savedUser.getRole());
        return vo;
    }

    @Transactional
    public AuthUserVO registerAdmin(String phone, String password) {
        if (userAccountRepository.findByPhone(phone).isPresent()) {
            throw new BusinessException(409, "手机号已注册");
        }
        UserAccount user = new UserAccount();
        user.setPhone(phone);
        user.setRole("ROLE_ADMIN");
        user.setPasswordHash(passwordEncoder.encode(password));
        UserAccount savedUser = userAccountRepository.save(user);

        AuthUserVO vo = new AuthUserVO();
        vo.setUserId(savedUser.getUserId());
        vo.setPhone(savedUser.getPhone());
        vo.setName("管理员");
        vo.setRole(savedUser.getRole());
        return vo;
    }

    public AuthUserVO login(AuthLoginDTO dto) {
        String phone = dto.getPhone() == null ? null : dto.getPhone().trim();
        LoginLog log = new LoginLog();
        log.setUserId(0);
        log.setUsername(phone);
        log.setLoginTime(LocalDateTime.now());
        // log.setIpAddress(request.getRemoteAddr()); // Can pass in later
        
        try {
            if (phone == null || phone.isEmpty()) {
                throw new BusinessException(400, "手机号不能为空");
            }
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                throw new BusinessException(400, "密码不能为空");
            }
    
            UserAccount user = userAccountRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(401, "手机号或密码错误"));
    
            log.setUserId(user.getUserId());
            log.setRole(user.getRole());
            
            if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
                throw new BusinessException(401, "手机号或密码错误");
            }
            if (user.getStatus() == null || user.getStatus() != 1) {
                throw new BusinessException(403, "账号不可用");
            }
    
            AuthUserVO vo = new AuthUserVO();
            vo.setUserId(user.getUserId());
            vo.setPhone(user.getPhone());
            vo.setRole(user.getRole());
            vo.setDoctorId(user.getDoctorId());
            
            // if (user.getPid() != null) {
            //     vo.setPid(user.getPid());
            //     patientRepository.findById(user.getPid()).ifPresent(p -> vo.setName(p.getName()));
            // } else if (user.getDoctorId() != null) {
            if (user.getDoctorId() != null) {
                vo.setDoctorId(user.getDoctorId());
                doctorRepository.findById(user.getDoctorId()).ifPresent(d -> vo.setName(d.getName()));
            } else {
                if ("ROLE_PATIENT".equals(user.getRole())) {
                    // 获取患者信息和就诊卡号
                    com.his.server.entity.Patient patient = patientService.getCurrentPatient(user.getUserId());
                    if (patient != null) {
                        vo.setPid(patient.getPid());
                        vo.setCardNumber(patient.getCardNumber());
                        vo.setName(patient.getName());
                    } else {
                        vo.setName("用户" + user.getPhone().substring(7));
                    }
                } else {
                    vo.setName("管理员");
                }
            }
            
            log.setStatus("SUCCESS");
            loginLogRepository.save(log);
            return vo;
        } catch (Exception e) {
            log.setStatus("FAIL");
            log.setMessage(e.getMessage());
            loginLogRepository.save(log);
            throw e;
        }
    }

    /**
     * 根据用户ID获取完整用户信息
     */
    public AuthUserVO getUserInfo(Integer userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            return null;
        }

        AuthUserVO vo = new AuthUserVO();
        vo.setUserId(user.getUserId());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setDoctorId(user.getDoctorId());

        // 获取医生信息
        if (user.getDoctorId() != null) {
            doctorRepository.findById(user.getDoctorId()).ifPresent(d -> {
                vo.setDoctorId(d.getDoctorId());
                vo.setName(d.getName());
            });
        } else if ("ROLE_PATIENT".equals(user.getRole())) {
            // 获取患者信息
            com.his.server.entity.Patient patient = patientService.getCurrentPatient(userId);
            if (patient != null) {
                vo.setPid(patient.getPid());
                vo.setCardNumber(patient.getCardNumber());
                vo.setName(patient.getName());
            } else {
                vo.setName("用户" + user.getPhone().substring(7));
            }
        } else {
            vo.setName("管理员");
        }

        return vo;
    }

    /**
     * 获取登录日志
     */
    public java.util.List<LoginLog> getLoginLogs() {
        return loginLogRepository.findAll();
    }
}
