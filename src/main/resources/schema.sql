CREATE TABLE genres (
  genre_id INTEGER PRIMARY KEY,
  genre_name VARCHAR(255)
);

CREATE TABLE films (
  id INTEGER PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(1000),
  release_date DATE,
  duration INTEGER,
  mpa_id INTEGER
);

CREATE TABLE film_genres (
  film_id INTEGER NOT NULL,
  genre_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE motion_picture_association (
  mpa_id INTEGER PRIMARY KEY,
  mpa_name VARCHAR(255)
);

CREATE TABLE users (
  user_id INTEGER PRIMARY KEY,
  email VARCHAR(255),
  login VARCHAR(255),
  name VARCHAR(255),
  birthdate DATE
);

CREATE TABLE likes (
  film_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  like_timestamp TIMESTAMP,
  PRIMARY KEY (film_id, user_id)
);

CREATE TABLE friends (
  user_id INTEGER NOT NULL,
  friend_id INTEGER NOT NULL,
  PRIMARY KEY (user_id, friend_id)
);

ALTER TABLE film_genres ADD FOREIGN KEY (film_id) REFERENCES films (id);

ALTER TABLE film_genres ADD FOREIGN KEY (genre_id) REFERENCES genres (genre_id);

ALTER TABLE films ADD FOREIGN KEY (mpa_id) REFERENCES motion_picture_association (mpa_id);

ALTER TABLE likes ADD FOREIGN KEY (film_id) REFERENCES films (id);

ALTER TABLE likes ADD FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE friends ADD FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE friends ADD FOREIGN KEY (friend_id) REFERENCES users (user_id);