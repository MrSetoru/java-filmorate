package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        log.info("Отправлен список всех фильмов: {}", films.values());
        return films.values();
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не должно быть пустым");
        }
        film.setId(getNextId());
        film.setName(film.getName());
        film.setDescription(film.getDescription());
        film.setReleaseDate(film.getReleaseDate());
        film.setDuration(film.getDuration());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable Long id, @Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма с id {}: {}", id, newFilm);
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            log.warn("Фильм с id {} не найден", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (newFilm.getName() != null) {
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            oldFilm.setDuration(newFilm.getDuration());
        }
        films.put(id, oldFilm);
        log.info("Фильм с id {} успешно обновлен: {}", id, oldFilm);
        return new ResponseEntity<>(oldFilm, HttpStatus.OK);
    }


    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
