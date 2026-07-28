package com.smarttravel.common.config;

import com.smarttravel.common.dto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.fail(e.getMessage());
    }
}
