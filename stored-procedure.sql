DELIMITER //

CREATE PROCEDURE add_movie(
IN movie_title VARCHAR(100), 
IN input_year INT, 
IN input_director VARCHAR(100), 
IN star_name VARCHAR(100), 
IN birth_year INT, 
IN genre VARCHAR(32)
)
proc_label: BEGIN

DECLARE movie_count int;
DECLARE num_index int DEFAULT 1;
DECLARE movie_id VARCHAR(10);
DECLARE prefix VARCHAR(8);
DECLARE numeric_part INT;
DECLARE return_string VARCHAR(255);
DECLARE star_count int;
DECLARE star_id VARCHAR(10);
DECLARE genre_count int;
DECLARE genre_id INT;

SELECT count(*) into movie_count from movies where title = movie_title and year = input_year and director = input_director;

if movie_count > 0 THEN 
SELECT 'Movie already exists' AS RESULT;
LEAVE proc_label;
end if;

SELECT max(id) into movie_id from movies;

firstloop: WHILE (num_index <= LENGTH(movie_id))
DO
if SUBSTRING(movie_id, num_index, 1) REGEXP '[0-9]' THEN
LEAVE firstloop; 
END IF;
SET num_index = num_index + 1;
END WHILE firstloop;

SET prefix = SUBSTRING(movie_id, 1, num_index - 1);
SET numeric_part = CAST(SUBSTRING(movie_id, num_index) AS UNSIGNED);
SET numeric_part = numeric_part + 1;

SET movie_id = CONCAT(prefix, numeric_part);

INSERT INTO movies(id, title, year, director) values (movie_id, movie_title, input_year, input_director);

IF birth_year = 0 THEN
SELECT count(*) into star_count from stars where name = star_name and birthYear is NULL;
ELSE
SELECT count(*) into star_count from stars where name = star_name and birthYear = birth_year;
end if;

if star_count > 0 THEN
IF birth_year = 0 THEN
SELECT id into star_id from stars where name = star_name and birthYear is NULL;
else
select id into star_id from stars where name = star_name and birthYear = birth_year;
end if;
else
SELECT max(id) into star_id from stars;

SET num_index = 1;
secondloop: WHILE num_index <= LENGTH(star_id) DO
if SUBSTRING(star_id, num_index, 1) REGEXP '[0-9]' THEN
LEAVE secondloop;
END IF;
SET num_index = num_index + 1;
END WHILE secondloop;

SET prefix = SUBSTRING(star_id, 1, num_index - 1);
SET numeric_part = CAST(SUBSTRING(star_id, num_index) AS UNSIGNED);
set numeric_part = numeric_part + 1;

set star_id = CONCAT(prefix, numeric_part);
INSERT INTO stars(id, name, birthYear) values (star_id, star_name, birth_year);
end if;

INSERT INTO stars_in_movies(starId, movieId) values (star_id, movie_id);

select count(*) into genre_count from genres where name = genre;

IF genre_count > 0 THEN
SELECT id INTO genre_id FROM genres where name = genre;
ELSE
SELECT max(id) INTO genre_id from genres;
SET genre_id = genre_id + 1;
INSERT INTO genres(id, name) values (genre_id, genre);
END IF;

INSERT INTO genres_in_movies(genreId, movieId) values (genre_id, movie_id);

SELECT CONCAT('movieID: ', movie_id, ', starID: ', star_id, ', genreID: ', genre_id) as RESULT;

END //

DELIMITER ;