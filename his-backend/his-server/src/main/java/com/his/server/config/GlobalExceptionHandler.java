package com.his.server.config;

import com.his.common.result.GlobalResult;
import com.his.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public GlobalResult<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return GlobalResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public GlobalResult<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return GlobalResult.error("系统繁忙，请稍后重试: " + e.getMessage());
    }
}
