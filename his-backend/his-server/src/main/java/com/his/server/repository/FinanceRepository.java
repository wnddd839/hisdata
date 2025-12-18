package com.his.server.repository;

import com.his.server.entity.Finance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinanceRepository extends JpaRepository<Finance, Integer> {
    List<Finance> findByAppointmentId(Integer appointmentId);
    
    @Query("SELECT COALESCE(SUM(f.totalFee), 0) FROM Finance f WHERE f.paymentStatus = :status AND f.paymentTime BETWEEN :start AND :end")
    BigDecimal sumTotalFeeByPaymentStatusAndPaymentTimeBetween(@Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
