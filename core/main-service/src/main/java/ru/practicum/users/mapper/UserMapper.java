package ru.practicum.users.mapper;


import org.mapstruct.Mapper;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.dto.UserShortDto;
import ru.practicum.users.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserShortDto mapUserToUserShortDto(User user);

    User mapNewUserToUser(NewUserRequest newUser);

    UserDto mapUserToUserDto(User user);

    List<UserDto> mapUsersToUsersDto(List<User> users);
}
