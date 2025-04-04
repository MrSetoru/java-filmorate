package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validator.ReleaseDateConstraint;

import java.time.Instant;

@Data
@EqualsAndHashCode(of = {"name", "releaseDate", "duration"})
public class Film {
    private Long id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;
    @ReleaseDateConstraint
    private Instant releaseDate;
    /**
     * Продолжительность фильма в минутах.
     */
    @Min(value = 1, message = "Продолжительность фильма должна быть положительной")
    private long duration;
}
