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
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage; // Добавлена зависимость от UserStorage

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
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
        User user = userStorage.getUserById(userId); // Проверяем, что пользователь существует

        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка: filmId={}, userId={}", filmId, userId);
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId); // Проверяем, что пользователь существует

        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        return filmStorage.findAll().stream()
                .sorted((film1, film2) -> Long.compare(film2.getLikes().size(), film1.getLikes().size())) // Сортируем по количеству лайков
                .limit(count)
                .collect(Collectors.toList());
    }
}