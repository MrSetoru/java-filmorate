package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
@Qualifier("userDbStorage")
@RequiredArgsConstructor
@Slf4j
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    @Override
    public Collection<User> findAllUsers() {
        String sql = "SELECT user_id, email, login, name, birthday FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setDate(4, Date.valueOf(user.getBirthday()));
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            log.warn("Попытка создать пользователя с уже зарегистрированным email: {}", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Пользователь с id {} успешно создан", user.getId());
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getId());

        if (rowsAffected == 0) {
            log.warn("Пользователь с id {} не найден", newUser.getId());
            throw new UserNotFoundException("Пользователь с id " + newUser.getId() + " не найден.");
        }
        log.info("Пользователь с id {} успешно обновлен", newUser.getId());
        return getUserById(newUser.getId());
    }

    @Override
    public User getUserById(Long id) {
        String sql = "SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?";

        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);

        if (users.isEmpty()) {
            log.warn("Пользователь с id {} не найден", id);
            throw new UserNotFoundException("Пользователь с id " + id + " не найден.");
        }
        return users.get(0);
    }

    @Override
    public void deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected == 0) {
            log.warn("Пользователь с id {} не найден", id);
            throw new UserNotFoundException("Пользователь с id " + id + " не найден.");
        }
        log.info("Пользователь с id {} успешно удален", id);
    }
}