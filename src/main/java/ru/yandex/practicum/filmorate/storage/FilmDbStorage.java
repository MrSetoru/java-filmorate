package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Component
@Qualifier("filmDbStorage")
@Slf4j
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(rs.getLong("mpa_id"));
        film.setMpa(mpaRating);

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
        long mpaId = film.getMpa().getId();

        String checkMpaSql = "SELECT COUNT(*) FROM motion_picture_association WHERE mpa_id = ?";
        Integer mpaCount = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, mpaId);

        if (mpaCount == null || mpaCount == 0) {
            log.error("Mpa with id {} not found", mpaId);
            throw new MpaNotFoundException("Mpa with id " + mpaId + " not found");
        }

        try {
            String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                ps.setLong(5, mpaId);
                return ps;
            }, keyHolder);

            film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
            log.info("Фильм с id {} успешно создан", film.getId());

            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                saveFilmGenres(film);
            }

            return film;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Ошибка при создании фильма (после проверки MPA).  " +
                    "Возможно, нарушены другие ограничения целостности.", e);
        }
    }

    @Override
    public Film updateFilm(Film newFilm) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
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
                "LIMIT :count";

        Map<String, Object> params = new HashMap<>();
        params.put("count", count);

        return namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
    }

    private void saveFilmGenres(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            batchArgs.add(new Object[]{film.getId(), genre.getId()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void updateFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film);
        }
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genre AS fg JOIN genres AS g ON fg.genre_id = g.genre_id WHERE film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        film.setGenres(new HashSet<>(genres));
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
