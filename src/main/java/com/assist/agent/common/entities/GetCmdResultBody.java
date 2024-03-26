package com.assist.agent.common.entities;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetCmdResultBody {
    @NotBlank(message = "任务id不能为空")
    private String taskId;
}
