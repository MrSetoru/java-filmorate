package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
    log.error("User not found: {}", e.getMessage());
    Map<String, String> errorResponse = Map.of("error", e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException e) {
    log.error("Resource not found: {}", e.getMessage());
    Map<String, String> errorResponse = Map.of("error", e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MpaNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleMpaNotFoundException(MpaNotFoundException e) {
    log.error("Mpa not found: {}", e.getMessage());
    Map<String, String> errorResponse = Map.of("error", e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(GenreNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleGenreNotFoundException(GenreNotFoundException e) {
    log.error("Genre not found: {}", e.getMessage());
    Map<String, String> errorResponse = Map.of("error", e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleException(Exception e) {
    log.error("Internal server error: {}", e.getMessage(), e);
    Map<String, String> errorResponse = Map.of("error", "Внутренняя ошибка сервера");
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
