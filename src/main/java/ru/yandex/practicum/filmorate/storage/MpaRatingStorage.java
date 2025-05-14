package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

public interface MpaRatingStorage {
    Optional<MpaRating> getMpaRatingById(Long id);

    Collection<MpaRating> getAllMpaRatings();
}