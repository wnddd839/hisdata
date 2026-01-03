package com.his.server.repository;

import com.his.server.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByPid(Integer pid);
    List<Appointment> findByPidOrPatientId(Integer pid, Integer patientId);
    List<Appointment> findByDoctorId(Integer doctorId);
    List<Appointment> findByDoctorIdAndRegistrationDate(Integer doctorId, LocalDate date);
    List<Appointment> findByDoctorIdAndRegistrationDateAndStatusOrderBySerialNumberAsc(Integer doctorId, LocalDate date, Integer status);
    long countByRegistrationDate(LocalDate date);
    long countByRegistrationDateAndStatus(LocalDate date, Integer status);
    List<Appointment> findByRegistrationDateOrderByRegistrationTimeDesc(LocalDate date);

    @Query("SELECT a.doctorId, COUNT(a) as cnt FROM Appointment a GROUP BY a.doctorId ORDER BY cnt DESC")
    List<Object[]> findTopDoctors(Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.pid IN (SELECT p.pid FROM Patient p WHERE p.userId = :userId)")
    List<Appointment> findByUserId(@Param("userId") Integer userId);
}
