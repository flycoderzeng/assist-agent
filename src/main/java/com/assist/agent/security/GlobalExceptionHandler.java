package com.assist.agent.security;

import com.assist.agent.common.entities.BaseResponse;
import com.assist.agent.utils.ResultUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public BaseResponse handleException(Exception e) {
        e.printStackTrace();
        return ResultUtils.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败");
    }
}