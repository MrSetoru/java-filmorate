package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidLoginValidator implements ConstraintValidator<ValidLogin, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        if (value.trim().isEmpty()) {
            return false;
        }
        if (value.contains(" ")) {
            return false;
        }
        return true;
    }
}