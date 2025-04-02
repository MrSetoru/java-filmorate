package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDateConstraint, Instant> {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(Instant value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        LocalDate releaseDate = value.atZone(ZoneId.of("UTC")).toLocalDate();
        boolean isValid = !releaseDate.isBefore(MIN_RELEASE_DATE);
        return isValid;
    }
}