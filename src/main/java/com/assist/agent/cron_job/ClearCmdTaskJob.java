package com.assist.agent.cron_job;

import com.assist.agent.controller.ShellExecuteController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClearCmdTaskJob {
    @Scheduled(cron = "00 */30 * * * ?")
    public void run() {
        log.info("开始执行 清理cmd执行任务");
        ShellExecuteController.CMD_TASK_MAP.forEach((key, value) -> {
            // 比超时时间多10分钟
            boolean isTimeout = (System.currentTimeMillis() - value.getStartTime()) > (value.getTimeout() * 60 * 1000 + 600 * 1000);
            if(isTimeout && !value.getFuture().isDone()) {
                value.getFuture().cancel(true);
            }
            if(isTimeout) {
                ShellExecuteController.CMD_TASK_MAP.remove(key);
            }
        });
    }
}
