package ru.practicum.yandex.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.user.dto.UserDto;
import ru.practicum.yandex.user.dto.NewUserRequest;
import ru.practicum.yandex.user.mapper.UserMapper;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

/**
 * Admin API for users
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@Validated
@Slf4j
public class UserAdminController {

    private final UserService userService;

    private final UserMapper userMapper;

    /**
     * Add new user. If added successfully, returns 201 response status.
     *
     * @param userShortDto new user parameters
     * @return added user
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest userShortDto) {
        log.info("Adding new user: '{}'.", userShortDto);
        User userToAdd = userMapper.toModel(userShortDto);
        User savedUser = userService.createUser(userToAdd);
        return userMapper.toDto(savedUser);
    }

    /**
     * Get information about users. If nothing found, returns empty list.
     *
     * @param ids  users ids to search in
     * @param from first element to display
     * @param size number of elements to display
     * @return found users
     */
    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") Long from,
                                  @RequestParam(defaultValue = "10") Integer size) {
        log.info("Requesting users, ids = '{}', from = '{}', size = '{}'.", ids, from, size);
        List<User> users = userService.getUsers(ids, from, size);
        return userMapper.toDtoList(users);
    }

    /**
     * Delete user by user id. If deleted successfully, returns 204 response status.
     *
     * @param userId user id to delete
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Deleting user with id = '{}'.", userId);
        userService.deleteUser(userId);
    }
}
