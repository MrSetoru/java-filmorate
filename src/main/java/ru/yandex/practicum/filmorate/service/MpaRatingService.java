package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

public interface MpaRatingService {
    MpaRating getMpaRatingById(Long id);

    Collection<MpaRating> getAllMpaRatings();
}