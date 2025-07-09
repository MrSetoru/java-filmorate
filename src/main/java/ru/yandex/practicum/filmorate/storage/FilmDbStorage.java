package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        log.info("Mapping row: {}", rowNum);
        Film film = new Film();
        try {
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));    MpaRating mpaRating = new MpaRating();
            mpaRating.setId(rs.getLong("mpa_id"));
            film.setMpa(mpaRating);

            return film;
        } catch (SQLException e) {
            log.error("Error mapping row", e);
            throw new RuntimeException(e);
        }

    };

    @Override
    public Collection<Film> findAllFilms() {
        log.info("Получен запрос на получение всех фильмов (из хранилища)");
        String sql = "SELECT id, name, description, release_date, duration, mpa_id FROM films";
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
                try {
                    saveFilmGenres(film);
                } catch (DataIntegrityViolationException e) {
                    if (e.getMessage().contains("FK_FILM_GENRES_GENRE")) {
                        throw new GenreNotFoundException("Жанр с указанным id не найден.");
                    } else {
                        throw new RuntimeException("Ошибка при создании фильма. " +
                                "Возможно, нарушены другие ограничения целостности.", e);
                    }
                }

            }

            return film;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Ошибка при создании фильма (после проверки MPA).  " +
                    "Возможно, нарушены другие ограничения целостности.", e);
        }

    }


    @Override
    public Film updateFilm(Film newFilm) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE id = ?";

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
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.mpa_name, " +
                "g.genre_id, g.genre_name " +
                "FROM films AS f " +
                "LEFT JOIN motion_picture_association AS m ON f.mpa_id = m.mpa_id " + // Добавляем JOIN
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, rs -> {
            List<Film> filmList = new ArrayList<>();
            Film currentFilm = null;
            while (rs.next()) {
                Long filmId = rs.getLong("id");
                if (currentFilm == null || currentFilm.getId() != filmId) {
                    if (currentFilm != null) {
                        filmList.add(currentFilm);
                    }
                    currentFilm = new Film();
                    currentFilm.setId(filmId);
                    currentFilm.setName(rs.getString("name"));
                    currentFilm.setDescription(rs.getString("description"));
                    currentFilm.setReleaseDate(rs.getDate("release_date").toLocalDate());
                    currentFilm.setDuration(rs.getInt("duration"));

                    MpaRating mpaRating = new MpaRating();
                    mpaRating.setId(rs.getLong("mpa_id"));
                    mpaRating.setName(rs.getString("mpa_name"));
                    currentFilm.setMpa(mpaRating);
                    currentFilm.setGenres(new HashSet<>());
                }
                if (rs.getLong("genre_id") != 0) {
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("genre_id"));
                    genre.setName(rs.getString("genre_name"));
                    currentFilm.getGenres().add(genre);
                }
            }
            if (currentFilm != null) {
                filmList.add(currentFilm);
            }
            return filmList;
        }, id);
        if (films.isEmpty()) {
            log.warn("Фильм с id {} не найден", id);
            throw new FilmNotFoundException("Фильм с id " + id + " не найден.");
        }
        return films.get(0);
    }

    @Override
    public void deleteFilm(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected == 0) {
            log.warn("Фильм с id {} не найден", id);
            throw new FilmNotFoundException("Фильм с id " + id + " не найден.");
        }
        log.info("Фильм с id {} успешно удален", id);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT :count";

        Map<String, Object> params = new HashMap<>();
        params.put("count", count);

        return namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
    }

    private void saveFilmGenres(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Genre> genres = new ArrayList<>(film.getGenres());

        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genres.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return genres.size();
                }
            });
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("FK_FILM_GENRES_GENRE_ID")) {
                throw new GenreNotFoundException("Один или несколько жанров не найдены.");
            } else {
                throw new RuntimeException("Ошибка при сохранении жанров фильма. " +
                        "Возможно, нарушены другие ограничения целостности.", e);
            }
        }
    }


    private void updateFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film);
        }
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres AS fg JOIN genres AS g ON fg.genre_id = g.genre_id WHERE film_id = ?";

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
