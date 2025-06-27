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

  private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
    log.error(message);
    Map<String, String> errorResponse = Map.of("error", message);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
    return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException e) {
    return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MpaNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleMpaNotFoundException(MpaNotFoundException e) {
    return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(GenreNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleGenreNotFoundException(GenreNotFoundException e) {
    return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
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
    return createErrorResponse("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
