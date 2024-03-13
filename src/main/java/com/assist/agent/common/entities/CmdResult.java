package com.assist.agent.common.entities;

import lombok.Data;

@Data
public class CmdResult {
    private Integer exitValue;
    private String output;

    public CmdResult(Integer exitValue, String output) {
        this.exitValue = exitValue;
        this.output = output;
    }
}
