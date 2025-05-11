package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        Collection<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        try {
            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Ошибка при создании пользователя: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);
        try {
            User updatedUser = userService.updateUser(user);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (NotFoundException e) {
            log.warn("Не удалось обновить пользователя, пользователь с id {} не найден", user.getId());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Пользователь с ID " + user.getId() + " не найден.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        log.info("Получен запрос на получение пользователя с ID: {}", id);
        try {
            User user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (NotFoundException e) {
            log.warn("Пользователь с ID {} не найден.", id);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Пользователь с ID " + id + " не найден.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на добавление в друзья: userId={}, friendId={}", id, friendId);
        try {
            userService.addFriend(id, friendId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            log.warn("Не удалось добавить пользователя {} в друзья к пользователю {}, один из пользователей не найден", friendId, id);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Пользователь не найден.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на удаление из друзей: userId={}, friendId={}", id, friendId);
        try {
            userService.removeFriend(id, friendId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            log.warn("Не удалось удалить пользователя {} из друзей пользователя {}, один из пользователей не найден", friendId, id);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Один из пользователей не найден.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<?> getFriends(@PathVariable Long id) {
        log.info("Получен запрос на получение списка друзей пользователя с ID: {}", id);
        try {
            Collection<User> friends = userService.getFriends(id);
            return new ResponseEntity<>(friends, HttpStatus.OK);
        } catch (NotFoundException e) {
            log.warn("Не удалось получить список друзей пользователя {}, пользователь не найден", id);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Пользователь с ID " + id + " не найден.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен запрос на получение общих друзей: userId={}, otherId={}", id, otherId);
        try {
            Collection<User> commonFriends = userService.getCommonFriends(id, otherId);
            return new ResponseEntity<>(commonFriends, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Не удалось получить список общих друзей пользователей {} и {}: {}", id, otherId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}