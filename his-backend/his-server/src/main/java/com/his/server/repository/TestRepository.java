package com.his.server.repository;

import com.his.server.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {
    List<Test> findByPid(Integer pid);
    List<Test> findByPidAndStatus(Integer pid, Integer status);
    List<Test> findByAppointmentId(Integer appointmentId);
    List<Test> findByStatus(Integer status);
    List<Test> findByDoctorIdAndStatus(Integer doctorId, Integer status);
    List<Test> findByDoctorId(Integer doctorId);
}