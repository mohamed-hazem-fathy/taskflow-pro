package com.taskflow.mapper;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.ProjectMember;
import com.taskflow.dto.response.ProjectMemberResponse;
import com.taskflow.dto.response.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMapper {

    @Mapping(target = "memberCount", constant = "0")
    @Mapping(target = "taskCount", constant = "0")
    ProjectResponse toResponse(Project project);

    ProjectMemberResponse toMemberResponse(ProjectMember member);
}
