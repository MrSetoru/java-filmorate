package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendshipService {

    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;

    public void addFriend(Long userId, Long friendId) {
        log.info("Запрос на добавление в друзья: userId={}, friendId={}", userId, friendId);

        userService.getUserById(userId);
        userService.getUserById(friendId);

        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Запрос на удаление из друзей: userId={}, friendId={}", userId, friendId);

        userService.getUserById(userId);
        userService.getUserById(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Запрос на получение общих друзей: userId={}, otherUserId={}", userId, otherUserId);

        userService.getUserById(userId);
        userService.getUserById(otherUserId);

        String sql = "SELECT f1.friend_id " +
                "FROM friends f1 " +
                "INNER JOIN friends f2 ON f1.friend_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";

        List<Long> commonFriendIds = jdbcTemplate.queryForList(sql, Long.class, userId, otherUserId);

        return commonFriendIds.stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getFriends(Long userId) {
        log.info("Запрос на получение друзей пользователя с ID: {}", userId);

        userService.getUserById(userId);

        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";

        List<Long> friendIds = jdbcTemplate.queryForList(sql, Long.class, userId);

        return friendIds.stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());
    }
}