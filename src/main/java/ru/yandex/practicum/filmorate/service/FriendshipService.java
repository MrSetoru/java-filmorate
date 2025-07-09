package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipStorage friendshipStorage;
    private final UserService userService;

    public void addFriend(Long userId, Long friendId) {
        log.info("Запрос на добавление в друзья: userId={}, friendId={}", userId, friendId);

        userService.getUserById(userId);
        userService.getUserById(friendId);

        friendshipStorage.addFriend(userId, friendId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Запрос на удаление из друзей: userId={}, friendId={}", userId, friendId);

        userService.getUserById(userId);
        userService.getUserById(friendId);

        friendshipStorage.removeFriend(userId, friendId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Запрос на получение общих друзей: userId={}, otherUserId={}", userId, otherUserId);

        userService.getUserById(userId);
        userService.getUserById(otherUserId);

        List<Long> commonFriendIds = friendshipStorage.getCommonFriendIds(userId, otherUserId);

        return commonFriendIds.stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getFriends(Long userId) {
        log.info("Запрос на получение друзей пользователя с ID: {}", userId);

        userService.getUserById(userId);

        List<Long> friendIds = friendshipStorage.getFriendIds(userId);

        return friendIds.stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());
    }
}
