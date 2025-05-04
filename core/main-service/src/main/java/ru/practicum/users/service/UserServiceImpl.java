package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.errors.exceptions.DataConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllOrderById(size, from);
        } else {
            users = userRepository.findAllById(ids);
        }

        return userMapper.mapUsersToUsersDto(users);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id =" + userId));
        userRepository.delete(user);
    }

    @Override
    public UserDto createUser(NewUserRequest newUser) {
        User user = userMapper.mapNewUserToUser(newUser);
        isEmailUnique(newUser.getEmail());
        return userMapper.mapUserToUserDto(userRepository.save(user));
    }

    private void isEmailUnique(String email) {
        boolean isEmailExists = userRepository.existsByEmail(email);
        if (isEmailExists) {
            throw new DataConflictException("User with this email already exists");
        }
    }

    @Override
    public User findUserById(Long userId) {
        User user = userRepository.getUserById(userId);
        return user;
    }
}
