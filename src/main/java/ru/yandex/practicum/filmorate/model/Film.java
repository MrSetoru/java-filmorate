package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validator.ReleaseDateConstraint;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"name", "releaseDate", "duration"})
public class Film {
    private Long id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;
    @ReleaseDateConstraint
    private LocalDate releaseDate;
    @Min(value = 1, message = "Продолжительность фильма должна быть положительной")
    private int duration;
    private Set<Long> likes = new HashSet<>();
    @NotNull(message = "Рейтинг MPA не может быть пустым")
    private Long mpaId;
    private Set<Long> genreIds = new HashSet<>();
}
