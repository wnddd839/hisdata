package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Appointment;
import com.his.server.entity.Patient;
import com.his.server.entity.UserAccount;
import com.his.server.repository.UserAccountRepository;
import com.his.server.service.AppointmentService;
import com.his.server.service.PatientService;
import com.his.server.service.PrescriptionService;
import com.his.server.service.TestService;
import com.his.server.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "医生工作台")
@RestController
@RequestMapping("/api/doctor-workstation")
@RequiredArgsConstructor
public class DoctorWorkstationController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final TestService testService;
    private final UserAccountRepository userAccountRepository;

    /**
     * 获取当前登录医生的doctorId
     */
    private Integer getCurrentDoctorId() {
        Integer userId = SecurityUtils.getCurrentUserId();
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new com.his.common.exception.BusinessException(404, "用户不存在"));
        if (user.getDoctorId() == null) {
            throw new com.his.common.exception.BusinessException(403, "非医生账号");
        }
        return user.getDoctorId();
    }

    @Data
    public static class PatientInfoVO {
        private Integer pid;
        private String name;
        private String gender;
        private Integer age;
        private String phone;
        private String idCard;
        private String address;
        private String allergy;
        private String cardNumber;

        public static PatientInfoVO from(Patient patient) {
            PatientInfoVO vo = new PatientInfoVO();
            vo.setPid(patient.getPid());
            vo.setName(patient.getName());
            // Convert Integer gender to String
            vo.setGender(patient.getGender() != null ? (patient.getGender() == 1 ? "男" : "女") : null);
            vo.setAge(patient.getAge());
            vo.setPhone(patient.getPhone());
            vo.setIdCard(patient.getIdCard());
            vo.setAddress(patient.getAddress());
            vo.setAllergy(patient.getAllergy());
            vo.setCardNumber(patient.getCardNumber());
            return vo;
        }
    }

    @Data
    public static class QueueItemVO {
        private Integer appointmentId;
        private Integer serialNumber;
        private String patientName;
        private String department;
        private String registrationTime;
        private Integer status; // 1=待就诊, 2=就诊中

        public static QueueItemVO from(Appointment apt, Patient patient) {
            QueueItemVO vo = new QueueItemVO();
            vo.setAppointmentId(apt.getAppointmentId());
            vo.setSerialNumber(apt.getSerialNumber());
            vo.setPatientName(patient.getName());
            vo.setDepartment(apt.getDepartment());
            vo.setRegistrationTime(apt.getRegistrationTime().toString());
            vo.setStatus(apt.getStatus());
            return vo;
        }
    }

    @Operation(summary = "获取医生今日概览")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Map<String, Object>> getOverview() {
        Integer doctorId = getCurrentDoctorId();

        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentService.listByDoctorAndDate(doctorId, today);

        long waitingCount = todayAppointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 1)
                .count();
        long inProgressCount = todayAppointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 2)
                .count();
        long completedCount = todayAppointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 3)
                .count();

        Map<String, Object> overview = new HashMap<>();
        overview.put("date", today.toString());
        overview.put("totalPatients", todayAppointments.size());
        overview.put("waitingCount", waitingCount);
        overview.put("inProgressCount", inProgressCount);
        overview.put("completedCount", completedCount);

        return GlobalResult.success(overview);
    }

    @Operation(summary = "获取当前排队队列")
    @GetMapping("/queue")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<List<QueueItemVO>> getQueue() {
        Integer doctorId = getCurrentDoctorId();

        // 获取今日待就诊和就诊中的患者
        List<Appointment> queue = appointmentService.getWaitingQueue(doctorId);

        List<QueueItemVO> result = queue.stream()
                .map(apt -> {
                    Patient patient = patientService.getById(apt.getPid());
                    return QueueItemVO.from(apt, patient);
                })
                .toList();

        return GlobalResult.success(result);
    }

    @Operation(summary = "获取医生候诊队列（前端专用接口）")
    @GetMapping("/my-queue")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<List<Appointment>> getMyQueue(
            @RequestParam Integer doctorId,
            @RequestParam(required = false) String date) {
        // 如果前端传了日期参数，使用该日期；否则使用今天
        LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        List<Appointment> queue = appointmentService.listByDoctorAndDate(doctorId, queryDate);

        // 按就诊序号排序，过滤掉已取消的
        List<Appointment> result = queue.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() != 0)
                .sorted((a, b) -> a.getSerialNumber().compareTo(b.getSerialNumber()))
                .toList();

        return GlobalResult.success(result);
    }

    @Operation(summary = "获取患者详情")
    @GetMapping("/patient/{pid}")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<PatientInfoVO> getPatientInfo(@PathVariable Integer pid) {
        Patient patient = patientService.getById(pid);
        if (patient == null) {
            return GlobalResult.error(404, "患者不存在");
        }
        return GlobalResult.success(PatientInfoVO.from(patient));
    }

    @Operation(summary = "获取医生所有挂号列表（包括未来日期）")
    @GetMapping("/all-appointments")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<List<Appointment>> getAllMyAppointments(@RequestParam Integer doctorId) {
        // 查询医生的所有挂号记录（不限制日期）
        List<Appointment> allAppointments = appointmentService.listByDoctor(doctorId);

        // 过滤掉已取消的，按日期倒序排列
        List<Appointment> result = allAppointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() != 0)
                .sorted((a, b) -> {
                    // 先按日期倒序
                    int dateCompare = b.getRegistrationDate().compareTo(a.getRegistrationDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    // 日期相同则按序号正序
                    return a.getSerialNumber().compareTo(b.getSerialNumber());
                })
                .toList();

        return GlobalResult.success(result);
    }

    @Operation(summary = "获取患者就诊历史")
    @GetMapping("/patient/{pid}/history")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Map<String, Object>> getPatientHistory(@PathVariable Integer pid) {
        List<Appointment> appointments = appointmentService.listByPatient(pid);

        Map<String, Object> history = new HashMap<>();
        history.put("appointments", appointments);
        history.put("totalVisits", appointments.size());

        return GlobalResult.success(history);
    }

    @Operation(summary = "开始就诊")
    @PostMapping("/appointment/{appointmentId}/start")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Appointment> startConsultation(@PathVariable Integer appointmentId) {
        Appointment appointment = appointmentService.updateStatus(appointmentId, 2);
        return GlobalResult.success(appointment);
    }

    @Operation(summary = "完成就诊")
    @PostMapping("/appointment/{appointmentId}/complete")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Appointment> completeConsultation(@PathVariable Integer appointmentId) {
        Appointment appointment = appointmentService.updateStatus(appointmentId, 3);
        return GlobalResult.success(appointment);
    }

    @Operation(summary = "叫号")
    @PostMapping("/appointment/{appointmentId}/call")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public GlobalResult<Void> callNumber(@PathVariable Integer appointmentId) {
        appointmentService.callPatient(appointmentId);
        return GlobalResult.success();
    }
}
