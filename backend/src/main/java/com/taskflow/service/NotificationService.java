package com.taskflow.service;

import com.taskflow.domain.entity.Notification;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.NotificationType;
import com.taskflow.dto.response.NotificationResponse;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.mapper.NotificationMapper;
import com.taskflow.repository.NotificationRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void createNotification(User recipient, NotificationType type, String title, String message,
                                   UUID referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification created: type={}, recipient={}", type, recipient.getEmail());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser, pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        return notificationRepository.countByRecipientAndIsReadFalse(currentUser);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to mark this notification as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        notificationRepository.markAllAsReadByRecipient(currentUser);
    }
}
