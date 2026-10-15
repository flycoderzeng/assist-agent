package com.assist.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.assist.agent.common.entities.RunCmdBody;
import com.assist.agent.common.entities.GetCmdResultBody;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShellExecuteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String taskId;

    @Test
    @Order(1)
    @WithMockUser(roles = "ADMIN")
    void testRunSyncCmd() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setCmd("cmd");
        runCmdBody.setParams(Arrays.asList("/c", "echo hello"));
        runCmdBody.setTimeout(1);

        mockMvc.perform(post("/shell/runSyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.exitValue").value(0))
                .andExpect(jsonPath("$.data.output").value("hello\n"));
    }

    @Test
    @Order(2)
    @WithMockUser(roles = "ADMIN")
    void testRunAsyncCmd() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setCmd("cmd");
        runCmdBody.setParams(Arrays.asList("/c", "echo hello async"));
        runCmdBody.setTimeout(1);

        String response = mockMvc.perform(post("/shell/runAsyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.taskStatus").value("running"))
                .andExpect(jsonPath("$.data.taskId").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        taskId = objectMapper.readTree(response).get("data").get("taskId").asText();
    }

    @Test
    @Order(3)
    @WithMockUser(roles = "ADMIN")
    void testGetCmdResult() throws Exception {
        Thread.sleep(2000);

        GetCmdResultBody getCmdResultBody = new GetCmdResultBody();
        getCmdResultBody.setTaskId(taskId);

        mockMvc.perform(post("/shell/getCmdResult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getCmdResultBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.taskId").value(taskId))
                .andExpect(jsonPath("$.data.taskStatus").value("finished"))
                .andExpect(jsonPath("$.data.exitValue").value(0))
                .andExpect(jsonPath("$.data.output").value("hello async\n"));
    }

    @Test
    @Order(10)
    @WithMockUser(roles = "ADMIN")
    void testRunSyncCmdMissingCmd() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setParams(Arrays.asList("/c", "echo hello"));
        runCmdBody.setTimeout(1);

        mockMvc.perform(post("/shell/runSyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(jsonPath("$.code").value(90000));
    }

    @Test
    @Order(11)
    @WithMockUser(roles = "ADMIN")
    void testRunSyncCmdTimeoutTooSmall() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setCmd("cmd");
        runCmdBody.setParams(Arrays.asList("/c", "echo hello"));
        runCmdBody.setTimeout(0);

        mockMvc.perform(post("/shell/runSyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(jsonPath("$.code").value(90000));
    }

    @Test
    @Order(12)
    @WithMockUser(roles = "ADMIN")
    void testRunSyncCmdTimeoutTooLarge() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setCmd("cmd");
        runCmdBody.setParams(Arrays.asList("/c", "echo hello"));
        runCmdBody.setTimeout(61);

        mockMvc.perform(post("/shell/runSyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(jsonPath("$.code").value(90000));
    }

    @Test
    @Order(13)
    @WithMockUser(roles = "ADMIN")
    void testRunSyncCmdInvalidCommand() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setCmd("nonexistent_command_xyz");
        runCmdBody.setTimeout(1);

        mockMvc.perform(post("/shell/runSyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(jsonPath("$.code").value(90000));
    }

    @Test
    @Order(14)
    @WithMockUser(roles = "ADMIN")
    void testRunAsyncCmdMissingCmd() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setParams(Arrays.asList("/c", "echo hello"));
        runCmdBody.setTimeout(1);

        mockMvc.perform(post("/shell/runAsyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(jsonPath("$.code").value(90000));
    }

    @Test
    @Order(15)
    @WithMockUser(roles = "ADMIN")
    void testRunAsyncCmdInvalidTimeout() throws Exception {
        RunCmdBody runCmdBody = new RunCmdBody();
        runCmdBody.setCmd("cmd");
        runCmdBody.setParams(Arrays.asList("/c", "echo hello"));
        runCmdBody.setTimeout(0);

        mockMvc.perform(post("/shell/runAsyncCmd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runCmdBody)))
                .andExpect(jsonPath("$.code").value(90000));
    }

    @Test
    @Order(16)
    @WithMockUser(roles = "ADMIN")
    void testGetCmdResultMissingTaskId() throws Exception {
        GetCmdResultBody getCmdResultBody = new GetCmdResultBody();

        mockMvc.perform(post("/shell/getCmdResult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getCmdResultBody)))
                .andExpect(jsonPath("$.code").value(90000));
    }

    @Test
    @Order(17)
    @WithMockUser(roles = "ADMIN")
    void testGetCmdResultNonExistentTaskId() throws Exception {
        GetCmdResultBody getCmdResultBody = new GetCmdResultBody();
        getCmdResultBody.setTaskId("non-existent-task-id-12345");

        mockMvc.perform(post("/shell/getCmdResult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getCmdResultBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(90000))
                .andExpect(jsonPath("$.message").value("任务不存在"));
    }
}