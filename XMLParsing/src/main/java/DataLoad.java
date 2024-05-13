import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class DataLoad {
    String securePath = "";

    public String setSecurePath() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
                    Parameters.username, Parameters.password);

            if (connection != null) {
                String secureQuery = "show variables like 'secure_file_priv';";
                Statement secureStatement = connection.createStatement();
                ResultSet secureRs = secureStatement.executeQuery(secureQuery);
                secureRs.next();
                securePath = secureRs.getString("value");
            }
        } catch (Exception e) {
        }

        return securePath;
    }
    public void runLoadData() {
        // create an instance
        MovieDomParse movieDomParse = new MovieDomParse();
        Set<String> movieIds = movieDomParse.runMovieDomParse();

        StarDomParse starDomParse = new StarDomParse();
        Map<String, ArrayList<Object>> actors = starDomParse.runStarDomParse();

        CastDomParse castDomParse = new CastDomParse();
        castDomParse.runCastDomParse(movieIds, actors);

//        try {
//            // Connect to the database
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection connection = DriverManager.getConnection("jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
//                    Parameters.username, Parameters.password);
//
//            if (connection != null) {
//                String secureQuery = "show variables like 'secure_file_priv';";
//                Statement secureStatement = connection.createStatement();
//                ResultSet secureRs = secureStatement.executeQuery(secureQuery);
//                secureRs.next();
//                securePath = secureRs.getString("value");
//
//                System.out.println("creating backup tables");
//                Statement tables = connection.createStatement();
//                String moviesTable = "CREATE TABLE IF NOT EXISTS movies_backup (\n" +
//                        "    id VARCHAR(10) PRIMARY KEY NOT NULL,\n" +
//                        "    title VARCHAR(100) NOT NULL,\n" +
//                        "    year INTEGER NOT NULL,\n" +
//                        "    director VARCHAR(100) NOT NULL\n" +
//                        ");";
//                tables.executeUpdate(moviesTable);
//
//                String starsTable = "CREATE TABLE IF NOT EXISTS stars_backup (\n" +
//                        "    id VARCHAR(10) PRIMARY KEY NOT NULL,\n" +
//                        "    name VARCHAR(100) NOT NULL,\n" +
//                        "    birthYear INTEGER DEFAULT NULL\n" +
//                        ");";
//                tables.executeUpdate(starsTable);
//
//                String simTable = "CREATE TABLE IF NOT EXISTS stars_in_movies_backup (\n" +
//                        "    starId VARCHAR(10) NOT NULL,\n" +
//                        "    movieId VARCHAR(10) NOT NULL,\n" +
//                        "    FOREIGN KEY (starId) REFERENCES stars_backup(id),\n" +
//                        "    FOREIGN KEY (movieId) REFERENCES movies_backup(id),\n" +
//                        "    PRIMARY KEY (starId, movieId)\n" +
//                        ");";
//                tables.executeUpdate(simTable);
//
//                String genresTable = "CREATE TABLE IF NOT EXISTS genres_backup (\n" +
//                        "    id INTEGER PRIMARY KEY AUTO_INCREMENT,\n" +
//                        "    name VARCHAR(32) NOT NULL\n" +
//                        ");";
//                tables.executeUpdate(genresTable);
//
//                String gimTable = "CREATE TABLE IF NOT EXISTS genres_in_movies_backup (\n" +
//                        "    genreId INTEGER NOT NULL,\n" +
//                        "    movieId VARCHAR(10) NOT NULL,\n" +
//                        "    FOREIGN KEY (genreId) REFERENCES genres_backup(id),\n" +
//                        "    FOREIGN KEY (movieId) REFERENCES movies_backup(id),\n" +
//                        "    PRIMARY KEY (genreId, movieId)\n" +
//                        ");";
//                tables.executeUpdate(gimTable);
//
//                System.out.println("Deleting old rows...");
//                tables.executeUpdate("SET FOREIGN_KEY_CHECKS=0;");
//                tables.executeUpdate("delete from movies_backup;");
//                tables.executeUpdate("delete from stars_backup;");
//                tables.executeUpdate("delete from stars_in_movies_backup;");
//                tables.executeUpdate("delete from genres_backup;");
//                tables.executeUpdate("delete from genres_in_movies_backup;");
//
//                tables.executeUpdate("insert into movies_backup select * from movies;");
//                tables.executeUpdate("insert into stars_backup select * from stars;");
//                tables.executeUpdate("insert into stars_in_movies_backup select * from stars_in_movies;");
//                tables.executeUpdate("insert into genres_backup select * from genres;");
//                tables.executeUpdate("insert into genres_in_movies_backup select * from genres_in_movies;");
//                tables.executeUpdate("SET FOREIGN_KEY_CHECKS=1;");


//                System.out.println("loading data into movies");
//                String query = "LOAD DATA LOCAL INFILE ? " +
//                        "IGNORE INTO TABLE movies " +
//                        "FIELDS TERMINATED BY ',' " +
//                        "OPTIONALLY ENCLOSED BY '\"' " +
//                        "LINES TERMINATED BY '\\n';";
//
//                PreparedStatement statement = connection.prepareStatement(query);
//                statement.setString(1, "mains_movies.txt");
//                statement.executeUpdate();
//                statement.close();
//
//                System.out.println("loading data into genres");
//                String query2 = "LOAD DATA LOCAL INFILE ? " +
//                        "IGNORE INTO TABLE genres " +
//                        "FIELDS TERMINATED BY ',' " +
//                        "LINES TERMINATED BY '\\n';";
//
//                PreparedStatement statement2 = connection.prepareStatement(query2);
//                statement2.setString(1, "mains_genres.txt");
//                statement2.executeUpdate();
//                statement2.close();
//
////                String oldGenres = "insert ignore into genres select * from genres;";
////                tables.executeUpdate(oldGenres);
//
//                System.out.println("loading data into stars");
//                String query3 = "LOAD DATA LOCAL INFILE ? " +
//                        "IGNORE INTO TABLE stars " +
//                        "FIELDS TERMINATED BY ','" +
//                        "LINES TERMINATED BY '\\n' " +
//                        "(id, name, @birth_year) " +
//                        "SET birthYear = NULLIF(@birth_year, 0);";
//
//                PreparedStatement statement3 = connection.prepareStatement(query3);
//                statement3.setString(1, "casts_stars.txt");
//                statement3.executeUpdate();
//                statement3.close();
//
//                System.out.println("loading data into gim");
//                String query4 = "LOAD DATA LOCAL INFILE ? " +
//                        "IGNORE INTO TABLE genres_in_movies " +
//                        "FIELDS TERMINATED BY ',' " +
//                        "LINES TERMINATED BY '\\n';";
//
//                PreparedStatement statement4 = connection.prepareStatement(query4);
//                statement4.setString(1, "mains_genres_in_movies.txt");
//                statement4.executeUpdate();
//                statement4.close();
//
//                System.out.println("loading data into sim");
//                String query5 = "LOAD DATA LOCAL INFILE ? " +
//                        "IGNORE INTO TABLE stars_in_movies " +
//                        "FIELDS TERMINATED BY ',' " +
//                        "LINES TERMINATED BY '\\n';";
//
//                PreparedStatement statement5 = connection.prepareStatement(query5);
//                statement5.setString(1, "casts_stars_in_movies.txt");
//                statement5.executeUpdate();
//                statement5.close();

//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        DataLoad dataLoad = new DataLoad();
        dataLoad.runLoadData();
    }
}
