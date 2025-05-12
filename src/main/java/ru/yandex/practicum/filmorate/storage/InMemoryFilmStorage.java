package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов (из хранилища)");
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.info("Добавление фильма в хранилище: {}", film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен в хранилище: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        log.info("Обновление фильма с id {} в хранилище, данные: {}", newFilm.getId(), newFilm);
        if (newFilm.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new IllegalArgumentException("Id фильма не может быть пустым");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден.");
        }
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id {} успешно обновлен в хранилище: {}", newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film getFilmById(Long id) {
        log.info("Получен запрос на получение фильма с id {}", id);
        if (!films.containsKey(id)) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        return films.get(id);
    }
}