package com.taskflow.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taskflow.annotation.Auditable;
import com.taskflow.domain.entity.AuditLog;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Set<String> SENSITIVE_FIELDS = Set.of("password", "passwordHash", "password_hash");

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        Object result = pjp.proceed();

        try {
            UUID userId = resolveCurrentUserId();
            String ipAddress = resolveClientIp();
            UUID entityId = extractEntityId(result, pjp.getArgs());
            String payload = buildPayload(pjp.getArgs());

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(auditable.action())
                    .entityType(auditable.entityType())
                    .entityId(entityId)
                    .payload(payload)
                    .ipAddress(ipAddress)
                    .build();

            auditLogService.saveAsync(auditLog);
        } catch (Exception e) {
            log.warn("Audit logging failed: {}", e.getMessage());
        }

        return result;
    }

    private UUID resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName())
                .map(user -> user.getId())
                .orElse(null);
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private UUID extractEntityId(Object result, Object[] args) {
        UUID fromResult = extractIdFromObject(result);
        if (fromResult != null) {
            return fromResult;
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof UUID uuid) {
                    return uuid;
                }
                UUID nestedId = extractIdFromObject(arg);
                if (nestedId != null) {
                    return nestedId;
                }
            }
        }
        return null;
    }

    private UUID extractIdFromObject(Object object) {
        if (object == null) {
            return null;
        }
        try {
            Method getId = object.getClass().getMethod("getId");
            Object value = getId.invoke(object);
            if (value instanceof UUID uuid) {
                return uuid;
            }
        } catch (ReflectiveOperationException ignored) {
            // try record-style id()
        }
        try {
            Method id = object.getClass().getMethod("id");
            Object value = id.invoke(object);
            if (value instanceof UUID uuid) {
                return uuid;
            }
        } catch (ReflectiveOperationException ignored) {
            // no id available
        }
        return null;
    }

    private String buildPayload(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (Object arg : args) {
                if (arg == null) {
                    arrayNode.addNull();
                    continue;
                }
                ObjectNode node = objectMapper.valueToTree(arg);
                removeSensitiveFields(node);
                arrayNode.add(node);
            }
            return objectMapper.writeValueAsString(arrayNode);
        } catch (Exception e) {
            log.debug("Failed to serialize audit payload: {}", e.getMessage());
            return null;
        }
    }

    private void removeSensitiveFields(ObjectNode node) {
        SENSITIVE_FIELDS.forEach(node::remove);
        node.fields().forEachRemaining(entry -> {
            if (entry.getValue().isObject()) {
                removeSensitiveFields((ObjectNode) entry.getValue());
            }
        });
    }
}
