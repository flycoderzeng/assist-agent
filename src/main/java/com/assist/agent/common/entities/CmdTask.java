package com.assist.agent.common.entities;

import lombok.Data;
import org.zeroturnaround.exec.ProcessResult;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

@Data
public class CmdTask {
    private String taskId;
    private ConcurrentLinkedQueue<String> outputQueue;
    private Future<ProcessResult> future;
    private long startTime = System.currentTimeMillis();
    private int timeout = 10;

    public CmdTask() {
    }

    public CmdTask(String taskId, ConcurrentLinkedQueue outputQueue, int timeout) {
        this.taskId = taskId;
        this.outputQueue = outputQueue;
        this.timeout = timeout;
    }
}
