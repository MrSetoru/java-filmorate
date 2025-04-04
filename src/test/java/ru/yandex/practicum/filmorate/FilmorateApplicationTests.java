package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testValidFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Должна быть пустая коллекция нарушений для валидного фильма");
    }

    @Test
    void testFilmNameBlank() {
        Film film = new Film();
        film.setName("  ");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        film.setDuration(120);

        Set violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для фильма с пустым именем");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testFilmNameNull() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для фильма с null именем");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Название не может быть пустым", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testFilmDescriptionLong() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Very long description ".repeat(20)); // Очень длинное описание
        film.setReleaseDate(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для фильма с длинным описанием");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Описание не должно превышать 200 символов", violation.getMessage());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    void testFilmReleaseDateInvalid() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1800, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        film.setDuration(120);

        Set violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для фильма с неверной датой релиза");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testFilmDurationNegative() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        film.setDuration(-10);

        Set violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для фильма с отрицательной длительностью");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testFilmDurationZero() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        film.setDuration(0);

        Set violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для фильма с нулевой длительностью");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Должна быть пустая коллекция нарушений для валидного пользователя");
    }

    @Test
    void testInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для пользователя с неверным email");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testNullEmail() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для пользователя с null email");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testNullLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для пользователя с null логином");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testBlankLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для пользователя с пустым логином");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testLoginWithSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test Login");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для пользователя с пробелами в логине");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для пользователя с датой рождения в будущем");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }

    @Test
    void testNullBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(null);

        Set violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения для пользователя с null датой рождения");
        assertEquals(1, violations.size(), "Должно быть одно нарушение");
    }
}