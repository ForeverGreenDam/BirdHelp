package com.greendam.birdhelp.exception;

import com.greendam.birdhelp.common.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author ForeverGreenDam
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return BaseResponse.error(ErrorCode.PARAMS_ERROR.getCode(), msg);
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.error("BusinessException", e);
        return BaseResponse.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handleBusinessException(RuntimeException e) {
        log.error("BusinessException", e);
        return BaseResponse.error(ErrorCode.SYSTEM_ERROR);
    }
}
