package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreStorage genreStorage;

    @Override
    public Genre getGenreById(Long id) {
        Optional<Genre> genreOptional = genreStorage.getGenreById(id);
        if (genreOptional.isEmpty()) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        return genreOptional.get();
    }

    @Override
    public Collection<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }
}