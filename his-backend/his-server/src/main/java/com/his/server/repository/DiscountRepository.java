package com.his.server.repository;

import com.his.server.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    Discount findByDiscountCode(String discountCode);

    List<Discount> findByValidFromLessThanEqualAndValidUntilGreaterThanEqual(LocalDate from, LocalDate until);
}

