package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return users.values();
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        if (isEmailAlreadyRegistered(user.getEmail())) {
            throw new DuplicatedDataException("Этот email уже используется");
        }
        user.setId(getNextId());
        user.setEmail(user.getEmail());
        user.setLogin(user.getLogin());
        user.setName(user.getName());
        user.setBirthday(user.getBirthday());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: {}", user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable Long id, @Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя с id: {}, данные: {}", id, newUser);
        User oldUser = users.get(id);
        if (oldUser == null) {
            log.warn("Пользователь с id {} не найден", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!newUser.getEmail().equals(oldUser.getEmail()) && isEmailAlreadyRegistered(newUser.getEmail())) {
            throw new DuplicatedDataException("Этот email уже используется");
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        users.put(id, oldUser);
        log.info("Пользователь с id {} успешно обновлен: {}", id, oldUser);
        return new ResponseEntity<>(oldUser, HttpStatus.OK);
    }

    private boolean isEmailAlreadyRegistered(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
