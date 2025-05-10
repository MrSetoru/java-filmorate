package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(@Valid Film film);

    Film updateFilm(@Valid Film newFilm);

    Film getFilmById(Long id);
}