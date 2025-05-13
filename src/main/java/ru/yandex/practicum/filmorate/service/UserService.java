package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.findAllUsers();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(Long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return user;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Запрос на добавление в друзья: userId={}, friendId={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Запрос на удаление из друзей: userId={}, friendId={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Запрос на получение общих друзей: userId={}, otherUserId={}", userId, otherUserId);
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);

        Set<Long> userFriends = new HashSet<>(user.getFriends());
        Set<Long> otherUserFriends = new HashSet<>(otherUser.getFriends());

        userFriends.retainAll(otherUserFriends);

        return userFriends.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getFriends(Long id) {
        log.info("Запрос на получение друзей пользователя с ID: {}", id);
        User user = getUserById(id);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}