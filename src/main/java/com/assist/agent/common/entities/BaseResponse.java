package com.assist.agent.common.entities;

import lombok.Data;

@Data
public class BaseResponse {
    private Integer code;
    private String message;
    private Object data;
}
