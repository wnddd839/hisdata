package com.his.server.service;

import com.his.common.exception.BusinessException;
import com.his.server.entity.Discount;
import com.his.server.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;

    public List<Discount> listActive() {
        LocalDate today = LocalDate.now();
        return discountRepository.findByValidFromLessThanEqualAndValidUntilGreaterThanEqual(today, today);
    }

    public Discount getValidByCode(String discountCode) {
        Discount discount = discountRepository.findByDiscountCode(discountCode);
        if (discount == null) {
            throw new BusinessException("优惠码不存在");
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(discount.getValidFrom()) || today.isAfter(discount.getValidUntil())) {
            throw new BusinessException("优惠码已过期或未生效");
        }
        return discount;
    }

    @Transactional
    public Discount save(Discount discount) {
        LocalDate from = discount.getValidFrom();
        LocalDate until = discount.getValidUntil();
        if (from != null && until != null && until.isBefore(from)) {
            throw new BusinessException("优惠有效期结束时间不能早于开始时间");
        }
        return discountRepository.save(discount);
    }
}

