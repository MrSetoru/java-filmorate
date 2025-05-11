package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов");
        return new ResponseEntity<>(filmService.getAllFilms(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        return new ResponseEntity<>(filmService.createFilm(film), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        return new ResponseEntity<>(filmService.updateFilm(film), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с id: {}", id);
        Film film = filmService.getFilmById(id);
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка: filmId={}, userId={}", id, userId);
        try {
            filmService.addLike(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            log.warn("Не удалось добавить лайк: filmId={}, userId={}.  {}", id, userId, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Фильм или пользователь не найден.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка: filmId={}, userId={}", id, userId);
        try {
            filmService.removeLike(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            log.warn("Не удалось удалить лайк: filmId={}, userId={}. {}", id, userId, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Фильм или пользователь или лайк не найден.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        try {
            Collection<Film> popularFilms = filmService.getPopularFilms(count);
            return new ResponseEntity<>(popularFilms, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Ошибка при получении популярных фильмов: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}