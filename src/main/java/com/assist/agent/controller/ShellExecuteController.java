package com.assist.agent.controller;

import com.assist.agent.common.entities.BaseResponse;
import com.assist.agent.common.entities.CmdResult;
import com.assist.agent.common.entities.RunCmdBody;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Validated
@RestController
@RequestMapping(value = "/shell")
public class ShellExecuteController {
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/runSyncCmd", produces = {"application/json;charset=UTF-8"})
    public BaseResponse runCmd(@RequestBody @Valid RunCmdBody runCmdBody) {
        StringBuilder builder = new StringBuilder();
        Integer exitValue;
        try {
            ProcessResult execute = new ProcessExecutor().command(runCmdBody.getParamsArray()).environment(runCmdBody.getEnvironment())
                    .timeout(10, TimeUnit.MINUTES)
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
}
