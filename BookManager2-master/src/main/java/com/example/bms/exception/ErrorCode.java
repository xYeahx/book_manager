package com.example.bms.exception;

/**
 * 统一错误码枚举
 * 集中管理所有业务错误码，避免 magic number
 */
public enum ErrorCode {

    // ========== 通用 (200~499) ==========
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或会话已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    USERNAME_EMPTY(420, "用户名不能为空"),
    INVALID_CREDENTIALS(420, "账号或密码错误"),
    USER_NOT_FOUND(420, "用户不存在"),
    GET_USER_INFO_FAILED(420, "获取用户信息失败"),

    // ========== 用户管理 (-1 ~ -6) ==========
    NOT_LOGGED_IN(-1, "未登录或会话已过期"),
    CANNOT_OPERATE_SELF(-2, "不能操作自己"),
    PERMISSION_DENIED(-3, "权限不足"),
    SUPER_ADMIN_MINIMUM(-5, "系统至少需要保留一个超级管理员"),
    CANNOT_CHANGE_OWN_ROLE(-6, "不能修改自己的角色"),

    // ========== 借阅相关 (-1 ~ -4) ==========
    BOOK_ALREADY_RETURNED(-1, "图书已归还（无法续借）"),
    BORROW_NOT_EXIST(-2, "借阅记录不存在"),
    BOOK_OVERDUE(-3, "图书已逾期（无法续借）"),
    MAX_RENEW_REACHED(-4, "已达到最大续借次数"),
    BORROW_FAILED(0, "借书失败"),
    RETURN_FAILED(0, "还书失败"),

    // ========== 注册 ==========
    MISSING_INVITE_CODE(-1, "缺少邀请码"),
    INVALID_INVITE_CODE(-1, "邀请码无效"),
    USERNAME_EXISTS(-1, "用户名已存在"),
    REGISTER_FAILED(0, "注册失败"),

    // ========== 系统 ==========
    PASSWORD_WRONG(0, "旧密码不正确"),
    OPERATION_FAILED(500, "操作失败"),
    INTERNAL_ERROR(500, "系统内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
