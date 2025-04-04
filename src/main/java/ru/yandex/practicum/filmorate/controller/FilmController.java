package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private Long nextId = 1L;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        log.info("Отправлен список всех фильмов (количество: {})", films.size());
        return films.values();
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return ResponseEntity.ok(film);
    }

    @PutMapping()
    public ResponseEntity updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма с id: {}, данные: {}", newFilm.getId(), newFilm);
        if (newFilm.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            return new ResponseEntity<>("Id фильма не может быть пустым", HttpStatus.BAD_REQUEST);
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id {} не найден", newFilm.getId());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id {} успешно обновлен: {}", newFilm.getId(), newFilm);
        return new ResponseEntity<Film>(newFilm, HttpStatus.OK);
    }
}
