package com.his.server.controller;

import com.his.common.result.GlobalResult;
import com.his.server.entity.Discount;
import com.his.server.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "优惠管理")
@RestController
@RequestMapping("/api/discount")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @Operation(summary = "查询当前有效优惠")
    @GetMapping("/active")
    public GlobalResult<List<Discount>> listActive() {
        return GlobalResult.success(discountService.listActive());
    }

    @Operation(summary = "根据优惠码查询")
    @GetMapping("/code/{code}")
    public GlobalResult<Discount> getByCode(@PathVariable("code") String code) {
        return GlobalResult.success(discountService.getValidByCode(code));
    }

    @Operation(summary = "创建或更新优惠")
    @PostMapping
    public GlobalResult<Discount> save(@RequestBody Discount discount) {
        return GlobalResult.success(discountService.save(discount));
    }
}

