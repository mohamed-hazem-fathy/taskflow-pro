package com.taskflow.mapper;

import com.taskflow.domain.entity.Notification;
import com.taskflow.dto.response.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
