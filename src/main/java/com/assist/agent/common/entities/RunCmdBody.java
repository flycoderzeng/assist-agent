package com.assist.agent.common.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RunCmdBody {
    @NotNull(message = "命令不能为空")
    @NotBlank(message = "命令不能为空")
    private String cmd;
    // 命令参数列表
    private List<String> params;

    // 分钟
    private int timeout = 10;

    //specific environment
    private Map<String, String> env;

    public String[] getParamsArray() {
        int size = 1;
        if(params != null && !params.isEmpty()) {
            size += params.size();
        }
        String[] strings = new String[size];
        strings[0] = cmd;
        if(params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                strings[i+1] = params.get(i);
            }
        }
        return strings;
    }

    public Map<String, String> getEnvironment() {
        if(env != null && !env.isEmpty()) {
            return env;
        }
        return new HashMap<>();
    }
}
