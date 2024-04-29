import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String movie_id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting movie_id: " + movie_id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

//            SELECT t1.*, t2.rating, GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genre_names,
//            GROUP_CONCAT(DISTINCT s.name ORDER BY star_movie_count DESC, s.name) AS stars
//            FROM movies t1 LEFT JOIN ratings t2 ON t1.id = t2.movieId INNER JOIN genres_in_movies t3 ON t1.id = t3.movieId
//            INNER JOIN genres g ON t3.genreId = g.id INNER JOIN stars_in_movies t4 ON t1.id = t4.movieId
//            INNER JOIN ( SELECT starId, COUNT(*) AS star_movie_count FROM stars_in_movies GROUP BY starId ) star_counts ON t4.starId = star_counts.starId
//            INNER JOIN stars s ON t4.starId = s.id where t1.id = ? GROUP BY t1.id, t2.rating ORDER BY t2.rating DESC, t1.title;

            String query = "SELECT t1.*, t2.rating, GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genre_names,\n" +
                    "GROUP_CONCAT(DISTINCT s.name ORDER BY star_movie_count DESC, s.name) AS stars\n" +
                    "FROM movies t1 " +
                    "LEFT JOIN ratings t2 ON t1.id = t2.movieId " +
                    "INNER JOIN genres_in_movies t3 ON t1.id = t3.movieId " +
                    "INNER JOIN genres g ON t3.genreId = g.id " +
                    "INNER JOIN stars_in_movies t4 ON t1.id = t4.movieId " +
                    "INNER JOIN ( SELECT starId, COUNT(*) AS star_movie_count FROM stars_in_movies GROUP BY starId ) star_counts ON t4.starId = star_counts.starId\n" +
                    "INNER JOIN stars s ON t4.starId = s.id " +
                    "where t1.id = ? \n" +
                    "GROUP BY t1.id, t2.rating \n" +
                    "ORDER BY t2.rating DESC, t1.title;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, movie_id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieGenres = rs.getString("genre_names");
                String movieStars = rs.getString("stars");
                String movieRating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGenres);
                jsonObject.addProperty("movie_stars", movieStars);
                jsonObject.addProperty("movie_rating", movieRating);

                Statement stars_statement = conn.createStatement();
                String[] stars_list = movieStars.split(",");
                String stars_id_query = "SELECT id FROM stars JOIN stars_in_movies ON stars.id = stars_in_movies.starId WHERE name IN ('";
                for (int j = 0; j < stars_list.length; j++) {
                    if (j < stars_list.length - 1) {
                        stars_id_query += stars_list[j] + "', '";
                    } else {
                        stars_id_query += stars_list[j] + "')";
                    }
                }
                stars_id_query += "AND stars_in_movies.movieId = '" + movieId + "' ORDER BY FIELD(name, '";
                for (int j = 0; j < stars_list.length; j++) {
                    if (j < stars_list.length - 1) {
                        stars_id_query += stars_list[j] + "', '";
                    } else {
                        stars_id_query += stars_list[j] + "');";
                    }
                }
                ResultSet stars_id_set = stars_statement.executeQuery(stars_id_query);

                StringBuilder stars_ids = new StringBuilder();
                while (stars_id_set.next()) {
                    stars_ids.append(stars_id_set.getString("id")).append(",");
                }
                jsonObject.addProperty("stars_ids", stars_ids.toString());
                jsonArray.add(jsonObject);

                stars_id_set.close();
                stars_statement.close();
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}