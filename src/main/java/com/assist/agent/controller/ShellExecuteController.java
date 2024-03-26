package com.assist.agent.controller;

import com.assist.agent.common.entities.*;
import com.assist.agent.utils.ResultUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;


@Validated
@RestController
@RequestMapping(value = "/shell")
public class ShellExecuteController {
    public static final Map<String, CmdTask> CMD_TASK_MAP = new ConcurrentHashMap<>();

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/runSyncCmd", produces = {"application/json;charset=UTF-8"})
    public BaseResponse runSyncCmd(@RequestBody @Valid RunCmdBody runCmdBody) {
        StringBuilder builder = new StringBuilder();
        Integer exitValue;
        try {
            ProcessResult execute = new ProcessExecutor().command(runCmdBody.getParamsArray()).environment(runCmdBody.getEnvironment())
                    .timeout(runCmdBody.getTimeout(), TimeUnit.MINUTES)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            builder.append(line).append("\n");
                        }
                    })
                    .execute();
            exitValue = execute.getExitValue();
        } catch (IOException e) {
            e.printStackTrace();
            return ResultUtils.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ResultUtils.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        } catch (TimeoutException e) {
            e.printStackTrace();
            return ResultUtils.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        }
        return ResultUtils.success(new CmdResult(exitValue, builder.toString()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/runAsyncCmd", produces = {"application/json;charset=UTF-8"})
    public BaseResponse runAsyncCmd(@RequestBody @Valid RunCmdBody runCmdBody) {
        String taskId = String.valueOf(UUID.randomUUID());
        CMD_TASK_MAP.put(taskId, new CmdTask(taskId, new ConcurrentLinkedQueue<String>(), runCmdBody.getTimeout()));
        try {
            Future<ProcessResult> future = new ProcessExecutor().command(runCmdBody.getParamsArray()).environment(runCmdBody.getEnvironment())
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            CMD_TASK_MAP.get(taskId).getOutputQueue().add(line);
                        }
                    })
                    .start().getFuture();
            CMD_TASK_MAP.get(taskId).setFuture(future);
        } catch (IOException e) {
            e.printStackTrace();
            return ResultUtils.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        }
        return ResultUtils.success(new CmdResult(taskId, "running"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/getCmdResult", produces = {"application/json;charset=UTF-8"})
    public BaseResponse getCmdResult(@RequestBody @Valid GetCmdResultBody getCmdResultBody) {
        CmdTask cmdTask = CMD_TASK_MAP.get(getCmdResultBody.getTaskId());
        if(cmdTask == null) {
            return ResultUtils.error("任务不存在");
        }
        CmdResult cmdResult = new CmdResult();
        cmdResult.setTaskId(getCmdResultBody.getTaskId());
        if(cmdTask.getFuture().isDone()) {
            cmdResult.setTaskStatus("finished");
            try {
                cmdResult.setExitValue(cmdTask.getFuture().get().getExitValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }else{
            cmdResult.setTaskStatus("running");
        }
        if(cmdTask.getFuture().isDone()) {
            CMD_TASK_MAP.remove(getCmdResultBody.getTaskId());
        }
        boolean isTimeout = (System.currentTimeMillis() - cmdTask.getStartTime()) > (cmdTask.getTimeout() * 60 * 1000);
        if(isTimeout && !cmdTask.getFuture().isDone()) {
            cmdTask.getFuture().cancel(true);
            cmdResult.setTaskStatus("timeout");
            CMD_TASK_MAP.remove(getCmdResultBody.getTaskId());
        }
        ConcurrentLinkedQueue outputQueue = cmdTask.getOutputQueue();
        StringBuilder builder = new StringBuilder();
        while (!outputQueue.isEmpty()) {
            String line = (String) outputQueue.poll();
            builder.append(line).append("\n");
        }
        cmdResult.setOutput(builder.toString());

        return ResultUtils.success(cmdResult);
    }
}
