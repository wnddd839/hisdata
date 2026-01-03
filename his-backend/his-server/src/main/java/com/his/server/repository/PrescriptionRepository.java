package com.his.server.repository;

import com.his.server.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {
    List<Prescription> findByPid(Integer pid);
    List<Prescription> findByPidAndStatus(Integer pid, Integer status);
    List<Prescription> findByAppointmentId(Integer appointmentId);
    List<Prescription> findByStatus(Integer status);
}