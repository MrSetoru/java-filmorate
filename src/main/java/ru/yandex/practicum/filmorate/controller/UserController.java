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
    private Long nextId = 1L;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        log.info("Отправлен список всех пользователей (количество: {})", users.size());
        return users.values();
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        if (isEmailAlreadyRegistered(user.getEmail())) {
            throw new DuplicatedDataException("Этот email уже используется");
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: {}", user);
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity updateUser(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя с id: {}, данные: {}", newUser.getId(), newUser);
        if (newUser.getId() == null) {
            log.warn("Попытка обновления пользователя без указания id");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User existingUser = users.get(newUser.getId());
        if (!newUser.getId().equals(existingUser.getId())) {
            log.warn("Попытка изменить id пользователя при обновлении.  Старый id: {}, новый id: {}", existingUser.getId(), newUser.getId());
            return new ResponseEntity<>("Нельзя изменить id пользователя при обновлении.", HttpStatus.BAD_REQUEST);
        }
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id {} успешно обновлен: {}", newUser.getId(), newUser);
        return new ResponseEntity<User>(newUser, HttpStatus.OK);
    }

    private boolean isEmailAlreadyRegistered(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }
}
