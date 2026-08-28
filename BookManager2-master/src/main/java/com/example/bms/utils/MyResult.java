package com.example.bms.utils;

import com.example.bms.exception.ErrorCode;

import java.util.HashMap;

/**
 * 统一 API 响应工具
 *
 * 返回格式说明：
 * - 简单结果: { status, message, timestamp }
 * - 带数据:   { status, message, data, timestamp }
 * - 分页列表: { code, message, count, data }
 */
public class MyResult {

    // ========== 简单结果 ==========

    public static HashMap<String, Object> getResultMap(Integer status, String message) {
        return new HashMap<String, Object>() {{
            put("status", status);
            put("message", message);
            put("timestamp", System.currentTimeMillis());
        }};
    }

    // ========== 带数据 ==========

    public static HashMap<String, Object> getResultMap(Integer status, String message, Object data) {
        return new HashMap<String, Object>() {{
            put("status", status);
            put("message", message);
            put("data", data);
            put("timestamp", System.currentTimeMillis());
        }};
    }

    // ========== 分页列表 ==========

    public static HashMap<String, Object> getListResultMap(Integer status, String message, Integer count, Object data) {
        return new HashMap<String, Object>() {{
            put("code", status);
            put("message", message);
            put("count", count);
            put("data", data);
        }};
    }

    // ========== 快捷方法（配合 ErrorCode） ==========

    /** 操作成功，无返回数据 */
    public static HashMap<String, Object> success() {
        return getResultMap(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage());
    }

    /** 操作成功，带返回数据 */
    public static HashMap<String, Object> success(Object data) {
        return getResultMap(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /** 操作失败，使用 ErrorCode */
    public static HashMap<String, Object> error(ErrorCode errorCode) {
        return getResultMap(errorCode.getCode(), errorCode.getMessage());
    }

    /** 操作失败，使用 ErrorCode + 自定义消息 */
    public static HashMap<String, Object> error(ErrorCode errorCode, String detail) {
        return getResultMap(errorCode.getCode(), detail);
    }

    /** 操作失败，使用自定义消息 (500) */
    public static HashMap<String, Object> error(String message) {
        return getResultMap(ErrorCode.OPERATION_FAILED.getCode(), message);
    }
}
