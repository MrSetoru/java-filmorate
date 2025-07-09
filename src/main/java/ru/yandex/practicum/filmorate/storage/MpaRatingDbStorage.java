package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage implements MpaRatingStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<MpaRating> getMpaRatingById(Long id) {
        String sql = "SELECT mpa_id, mpa_name FROM motion_picture_association WHERE mpa_id = ?";
        List<MpaRating> mpaRatings = jdbcTemplate.query(sql, this::mapRowToMpaRating, id);
        return mpaRatings.isEmpty() ? Optional.empty() : Optional.of(mpaRatings.get(0));
    }

    @Override
    public Collection<MpaRating> getAllMpaRatings() {
        String sql = "SELECT mpa_id, mpa_name FROM motion_picture_association";
        return jdbcTemplate.query(sql, this::mapRowToMpaRating);
    }

    private MpaRating mapRowToMpaRating(ResultSet rs, int rowNum) throws SQLException {
        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(rs.getLong("mpa_id"));
        mpaRating.setName(rs.getString("mpa_name"));
        return mpaRating;
    }
}