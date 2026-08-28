package com.example.bms.config;

import com.example.bms.exception.BusinessException;
import com.example.bms.exception.ErrorCode;
import com.example.bms.exception.NotEnoughException;
import com.example.bms.exception.OperationFailureException;
import com.example.bms.utils.MyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * 全局异常处理器
 * 统一捕获 Controller 层抛出的异常，返回标准 JSON 格式
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常 — 主动抛出的、携带明确错误码的异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Map<String, Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return MyResult.getResultMap(e.getCode(), e.getMessage());
    }

    /**
     * NotEnoughException — 库存不足（图书已借出）
     */
    @ExceptionHandler(NotEnoughException.class)
    @ResponseBody
    public Map<String, Object> handleNotEnoughException(NotEnoughException e) {
        log.warn("库存不足: {}", e.getMessage());
        return MyResult.getResultMap(500, e.getMessage());
    }

    /**
     * OperationFailureException — 操作失败
     */
    @ExceptionHandler(OperationFailureException.class)
    @ResponseBody
    public Map<String, Object> handleOperationFailureException(OperationFailureException e) {
        log.error("操作失败: {}", e.getMessage());
        return MyResult.getResultMap(500, e.getMessage());
    }

    /**
     * 缺少必需请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return MyResult.getResultMap(400, "缺少必需参数: " + e.getParameterName());
    }

    /**
     * 参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {} -> {}", e.getName(), e.getRequiredType());
        return MyResult.getResultMap(400, "参数 " + e.getName() + " 类型错误");
    }

    /**
     * 兜底：未捕获的其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        log.error("系统内部异常", e);
        return MyResult.getResultMap(500, "系统内部错误，请稍后重试");
    }
}
