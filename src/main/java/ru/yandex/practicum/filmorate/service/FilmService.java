package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAllFilms();
    }

    public Film createFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Запрос на добавление лайка: filmId={}, userId={}", filmId, userId);
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка: filmId={}, userId={}", filmId, userId);

        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        return filmStorage.getPopularFilms(count);
    }
}