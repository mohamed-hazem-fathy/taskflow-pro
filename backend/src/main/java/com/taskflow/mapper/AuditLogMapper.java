package com.taskflow.mapper;

import com.taskflow.domain.entity.AuditLog;
import com.taskflow.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
