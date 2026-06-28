package com.taskflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.domain.entity.Role;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.RoleType;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class TaskControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void fullTaskFlow_statusChangeCreatesHistory() throws Exception {
        String email = "taskflow@taskflow.com";
        String token = registerAndLogin(email);
        promoteToManager(email);

        String projectBody = """
                {"name":"Integration Project","description":"Test project"}
                """;
        String projectResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String projectId = objectMapper.readTree(projectResponse).path("data").path("id").asText();

        String taskBody = String.format("""
                {"title":"Integration Task","description":"Test task","priority":"HIGH","projectId":"%s"}
                """, projectId);
        String taskResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String taskId = objectMapper.readTree(taskResponse).path("data").path("id").asText();

        String updateBody = """
                {"status":"IN_PROGRESS"}
                """;
        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/" + taskId + "/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].fieldName").value("status"));
    }

    @Test
    void getProjectTasks_withStatusFilter_returnsOnlyMatchingTasks() throws Exception {
        String email = "filter@taskflow.com";
        String token = registerAndLogin(email);
        promoteToManager(email);

        String projectResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Filter Project\",\"description\":\"Filter test\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String projectId = objectMapper.readTree(projectResponse).path("data").path("id").asText();

        createTask(token, projectId, "Todo Task", "HIGH");
        String inProgressTaskResponse = createTask(token, projectId, "In Progress Task", "MEDIUM");
        String inProgressTaskId = objectMapper.readTree(inProgressTaskResponse).path("data").path("id").asText();

        mockMvc.perform(put("/api/tasks/" + inProgressTaskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/project/" + projectId + "?status=TODO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("TODO"));
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {"email":"%s","username":"%s","password":"password123","fullName":"Test User"}
                        """, email, email.split("@")[0]))).andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"email":"%s","password":"password123"}
                                """, email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResponse).path("data").path("token").asText();
    }

    private void promoteToManager(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Role managerRole = roleRepository.findByName(RoleType.MANAGER).orElseThrow();
        user.setRoles(Set.of(managerRole));
        userRepository.save(user);
    }

    private String createTask(String token, String projectId, String title, String priority) throws Exception {
        return mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"title":"%s","description":"desc","priority":"%s","projectId":"%s"}
                                """, title, priority, projectId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
