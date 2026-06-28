package com.taskflow.mapper;

import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.TaskComment;
import com.taskflow.domain.entity.TaskHistory;
import com.taskflow.dto.response.TaskCommentResponse;
import com.taskflow.dto.response.TaskHistoryResponse;
import com.taskflow.dto.response.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TaskMapper {

    @Mapping(target = "assignee", source = "assignee")
    TaskResponse toResponse(Task task);

    TaskCommentResponse toCommentResponse(TaskComment comment);

    TaskHistoryResponse toHistoryResponse(TaskHistory history);
}
