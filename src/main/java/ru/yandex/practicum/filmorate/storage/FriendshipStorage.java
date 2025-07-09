package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface FriendshipStorage {

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<Long> getCommonFriendIds(Long userId, Long otherUserId);

    List<Long> getFriendIds(Long userId);
}
