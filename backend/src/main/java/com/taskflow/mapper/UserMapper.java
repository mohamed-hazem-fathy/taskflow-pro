package com.taskflow.mapper;

import com.taskflow.domain.entity.User;
import com.taskflow.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    UserResponse toResponse(User user);

    default List<String> mapRoles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
    }
}
