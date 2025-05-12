package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();

    User createUser(@Valid User user);

    User updateUser(@Valid User user);

    User getUserById(Long id);
}