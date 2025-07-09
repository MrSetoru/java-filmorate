package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaRatingController {

    private final MpaRatingService mpaRatingService;

    @Autowired
    public MpaRatingController(MpaRatingService mpaRatingService) {
        this.mpaRatingService = mpaRatingService;
    }

    @GetMapping
    public ResponseEntity<Collection<MpaRating>> getAllMpaRatings() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        return new ResponseEntity<>(mpaRatingService.getAllMpaRatings(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaRating> getMpaRatingById(@PathVariable Long id) {
        log.info("Получен запрос на получение рейтинга MPA с id: {}", id);
        MpaRating mpaRating = mpaRatingService.getMpaRatingById(id);
        if (mpaRating == null) {
            log.warn("Рейтинг MPA с id {} не найден", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(mpaRating, HttpStatus.OK);
    }
}