package com.his.server.repository;

import com.his.server.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    List<Patient> findByNameContaining(String name);
    List<Patient> findByUserId(Integer userId);
    Optional<Patient> findByCardNumber(String cardNumber);
}
