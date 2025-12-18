package com.his.server.repository;

import com.his.server.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByPid(Integer pid);
    List<Appointment> findByPidOrPatientId(Integer pid, Integer patientId);
    List<Appointment> findByDoctorIdAndRegistrationDate(Integer doctorId, LocalDate date);
    long countByRegistrationDate(LocalDate date);

    @Query("SELECT a.doctorId, COUNT(a) as cnt FROM Appointment a GROUP BY a.doctorId ORDER BY cnt DESC")
    List<Object[]> findTopDoctors(Pageable pageable);
}
