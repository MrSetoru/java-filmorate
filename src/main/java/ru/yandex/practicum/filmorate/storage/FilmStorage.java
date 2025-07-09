package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> findAllFilms();

    Film create(@Valid Film film);

    Film updateFilm(@Valid Film newFilm);

    Film getFilmById(Long id);

    void deleteFilm(Long id);

    Collection<Film> getPopularFilms(int count);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}