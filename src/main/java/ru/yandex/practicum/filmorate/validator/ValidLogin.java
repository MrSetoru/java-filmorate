package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidLoginValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLogin {
    String message() default "Неверный формат логина. Логин не должен быть пустым и не должен содержать пробелы.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}