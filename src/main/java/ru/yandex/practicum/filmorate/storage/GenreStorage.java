package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {

    Optional<Genre> getGenreById(Long id);

    Collection<Genre> getAllGenres();
}