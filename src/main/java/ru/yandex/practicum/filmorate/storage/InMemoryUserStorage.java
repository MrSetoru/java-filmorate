package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей (из хранилища)");
        return users.values();
    }

    @Override
    public User createUser(User user) {
        log.info("Добавление пользователя в хранилище: {}", user);
        if (isEmailAlreadyRegistered(user.getEmail())) {
            log.warn("Попытка создать пользователя с уже зарегистрированным email: {}", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }
        if (user.getId() != null) {
            log.warn("При создании пользователя указан id, который будет проигнорирован.  Указанный id: {}", user.getId());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен в хранилище: {}", user);
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        log.info("Обновление пользователя с id {} в хранилище, данные: {}", newUser.getId(), newUser);
        if (newUser.getId() == null) {
            log.warn("Попытка обновления пользователя без указания id");
            throw new IllegalArgumentException("Id пользователя не может быть пустым");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с id {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден.");
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id {} успешно обновлен в хранилище: {}", newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User getUserById(Long id) {
        log.info("Получен запрос на получение пользователя с id {}", id);
        if (!users.containsKey(id)) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        return users.get(id);
    }

    private boolean isEmailAlreadyRegistered(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }
}