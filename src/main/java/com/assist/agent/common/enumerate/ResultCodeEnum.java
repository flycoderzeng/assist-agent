package com.assist.agent.common.enumerate;

public enum ResultCodeEnum {
    SUCCESS(0, "success"),
    SYSTEM_ERROR(90000, "系统异常"),
    ;
    private Integer code;

    private String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
