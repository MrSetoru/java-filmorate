package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
@Slf4j
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpaId(rs.getLong("mpa_id"));
        return film;
    };

    @Override
    public Collection<Film> findAllFilms() {
        log.info("Получен запрос на получение всех фильмов (из хранилища)");
        String sql = "SELECT film_id, name, description, release_date, duration, mpa_id FROM films";
        return jdbcTemplate.query(sql, filmRowMapper);
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpaId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Фильм с id {} успешно создан", film.getId());

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            saveFilmGenres(film);
        }

        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpaId(),
                newFilm.getId());

        if (rowsAffected == 0) {
            log.warn("Фильм с id {} не найден", newFilm.getId());
            throw new FilmNotFoundException("Фильм с id " + newFilm.getId() + " не найден.");
        }

        updateFilmGenres(newFilm);

        log.info("Фильм с id {} успешно обновлен", newFilm.getId());
        return getFilmById(newFilm.getId());
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = "SELECT film_id, name, description, release_date, duration, mpa_id FROM films WHERE film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);

        if (films.isEmpty()) {
            log.warn("Фильм с id {} не найден", id);
            throw new FilmNotFoundException("Фильм с id " + id + " не найден.");
        }

        Film film = films.get(0);
        loadFilmGenres(film);
        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected == 0) {
            log.warn("Фильм с id {} не найден", id);
            throw new FilmNotFoundException("Фильм с id " + id + " не найден.");
        }
        log.info("Фильм с id {} успешно удален", id);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, filmRowMapper, count);
    }

    private void saveFilmGenres(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Long genreId : film.getGenreIds()) {
            jdbcTemplate.update(sql, film.getId(), genreId);
        }
    }

    private void updateFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            saveFilmGenres(film);
        }
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        List<Long> genreIds = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.setGenreIds(new java.util.HashSet<>(genreIds));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id, like_timestamp) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, filmId, userId, new java.sql.Timestamp(System.currentTimeMillis()));
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

}