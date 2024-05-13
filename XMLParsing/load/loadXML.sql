LOAD DATA INFILE '/var/lib/mysql-files/load/mains_movies.txt'
IGNORE INTO TABLE movies
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n';

LOAD DATA INFILE '/var/lib/mysql-files/load/mains_genres.txt'
IGNORE INTO TABLE genres
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n';

LOAD DATA INFILE '/var/lib/mysql-files/load/casts_star.txt'
IGNORE INTO TABLE stars
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(id, name, @birth_year)
SET birthYear = NULLIF(@birth_year, 0);

LOAD DATA INFILE '/var/lib/mysql-files/load/mains_genres_in_movies.txt'
IGNORE INTO TABLE genres_in_movies
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n';

LOAD DATA INFILE '/var/lib/mysql-files/load/casts_stars_in_movies.txt'
IGNORE INTO TABLE stars_in_movies
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n';