package com.taskflow.controller;

import com.taskflow.dto.request.AddMemberRequest;
import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.ProjectMemberResponse;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create a project", description = "Creates a new project. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Project created successfully"));
    }

    @GetMapping
    @Operation(summary = "List my projects", description = "Returns paginated projects where the current user is owner or member")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getMyProjects(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        PagedResponse<ProjectResponse> response = PagedResponse.from(projectService.getMyProjects(pageable));
        return ResponseEntity.ok(ApiResponse.success(response, "Projects retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Returns project details. Requires project membership.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable UUID id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Project retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Partially updates a project. Requires OWNER or MANAGER role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Project updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Hard deletes a project. Only the OWNER can delete.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only owner can delete")
    })
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List project members", description = "Returns all members of a project. Requires project membership.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Members retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a project member")
    })
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable UUID id) {
        List<ProjectMemberResponse> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(ApiResponse.success(members, "Project members retrieved successfully"));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add project member", description = "Adds a user to the project. Requires OWNER or MANAGER role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Member added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or user not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already a member"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {
        ProjectMemberResponse response = projectService.addMember(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Member added successfully"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove project member", description = "Removes a user from the project. Cannot remove the OWNER.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or member not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions or cannot remove owner")
    })
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        projectService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed successfully"));
    }
}
