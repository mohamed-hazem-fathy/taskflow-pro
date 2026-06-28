package com.taskflow.controller;

import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.dto.TaskFilterParams;
import com.taskflow.dto.request.AddCommentRequest;
import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.TaskCommentResponse;
import com.taskflow.dto.response.TaskHistoryResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task in a project. Reporter is set to the current user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or assignee not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Task created successfully"));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "List project tasks", description = "Returns paginated tasks for a project with optional filters")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getProjectTasks(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateAfter,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        TaskFilterParams params = new TaskFilterParams();
        params.setStatus(status);
        params.setPriority(priority);
        params.setAssigneeId(assigneeId);
        params.setDueDateBefore(dueDateBefore);
        params.setDueDateAfter(dueDateAfter);
        params.setSearch(search);

        PagedResponse<TaskResponse> response = PagedResponse.from(
                taskService.getProjectTasks(projectId, params, pageable));
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Returns task details. Requires project membership.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable UUID id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Task retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Partially updates a task and automatically records change history")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Task updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task. Only reporter or project OWNER/MANAGER can delete.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully"));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add task comment", description = "Adds a comment to a task. Author is set to the current user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comment added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody AddCommentRequest request) {
        TaskCommentResponse response = taskService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Comment added successfully"));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List task comments", description = "Returns all comments for a task ordered by creation time")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comments retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getTaskComments(@PathVariable UUID id) {
        List<TaskCommentResponse> comments = taskService.getTaskComments(id);
        return ResponseEntity.ok(ApiResponse.success(comments, "Comments retrieved successfully"));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get task history", description = "Returns the change history for a task ordered by change time")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<List<TaskHistoryResponse>>> getTaskHistory(@PathVariable UUID id) {
        List<TaskHistoryResponse> history = taskService.getTaskHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history, "Task history retrieved successfully"));
    }
}
