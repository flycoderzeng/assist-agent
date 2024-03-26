package com.assist.agent.common.entities;

import lombok.Data;

@Data
public class CmdResult {
    private Integer exitValue;
    private String output;
    private String taskId;
    private String taskStatus;

    public CmdResult() {

    }

    public CmdResult(String taskId, String taskStatus) {
        this.taskId = taskId;
        this.taskStatus = taskStatus;
    }

    public CmdResult(Integer exitValue, String output) {
        this.exitValue = exitValue;
        this.output = output;
    }
}
