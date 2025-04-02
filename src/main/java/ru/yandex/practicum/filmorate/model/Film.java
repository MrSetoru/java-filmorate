package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validator.ReleaseDateConstraint;

import java.time.Duration;
import java.time.Instant;

@Data
@EqualsAndHashCode(of = {"name", "releaseDate", "duration"})
public class Film {
    @NotNull(message = "id не должно быть пустым")
    private Long id;
    @NotNull(message = "Название не может быть пустым")
    @Setter
    private String name;
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;
    @ReleaseDateConstraint
    private Instant releaseDate;
    private Duration duration;

    @Min(value = 1, message = "Продолжительность фильма должна быть положительной")
    private long durationInMinutes;

    public void setDuration(Duration duration) {
        this.duration = duration;
        this.durationInMinutes = duration.toMinutes(); // Преобразует Duration в минуты
    }

    public void setDurationInMinutes(long durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
        this.duration = Duration.ofMinutes(durationInMinutes); // Преобразует минуты обратно в Duration
    }
}
