package com.ne.template.users.mappers;


import com.ne.template.auth.dtos.RegisterRequestDto;
import com.ne.template.users.User;
import com.ne.template.users.dtos.UserResponseDto;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    User toEntity(RegisterRequestDto userDto);
    UserResponseDto toResponseDto(User user);
}
