package com.taskflow.controller;

import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.AuditLogResponse;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.service.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log endpoints (admin only)")
public class AuditController {

    private final AuditQueryService auditQueryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all audit logs", description = "Returns paginated audit logs. Admin only.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAllLogs(
            @PageableDefault(size = 20, sort = "occurredAt") Pageable pageable) {
        PagedResponse<AuditLogResponse> response = PagedResponse.from(auditQueryService.getAllLogs(pageable));
        return ResponseEntity.ok(ApiResponse.success(response, "Audit logs retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List audit logs by user", description = "Returns paginated audit logs for a specific user. Admin only.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "occurredAt") Pageable pageable) {
        PagedResponse<AuditLogResponse> response = PagedResponse.from(auditQueryService.getByUser(userId, pageable));
        return ResponseEntity.ok(ApiResponse.success(response, "Audit logs retrieved successfully"));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List audit logs by entity", description = "Returns paginated audit logs for a specific entity. Admin only.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @PageableDefault(size = 20, sort = "occurredAt") Pageable pageable) {
        PagedResponse<AuditLogResponse> response = PagedResponse.from(
                auditQueryService.getByEntity(entityType, entityId, pageable));
        return ResponseEntity.ok(ApiResponse.success(response, "Audit logs retrieved successfully"));
    }
}
