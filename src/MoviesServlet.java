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

// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();
//            SELECT
//                t1.*,
//                t2.rating,
//                SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name), ',', 3) AS genre_names,
//                SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name), ',', 3) AS stars
//            FROM
//                movies t1
//            JOIN
//                ratings t2 ON t1.id = t2.movieId
//            JOIN
//                genres_in_movies t3 ON t1.id = t3.movieId
//            JOIN
//                genres g ON t3.genreId = g.id
//            JOIN
//                stars_in_movies t4 ON t1.id = t4.movieId
//            JOIN
//                stars s ON t4.starId = s.id
            //GROUP BY
            //    t1.id,
            //    t2.rating
            //ORDER BY
            //    t2.rating DESC,
            //    t1.title
            //LIMIT 20;
            String query = "SELECT \n" +
                    "    t1.*,\n" +
                    "    t2.rating,\n" +
                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY SUBSTR(g.name, INSTR(g.name, ' '))), ',', 3) AS genre_names,\n" +
                    "    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY SUBSTR(s.name, INSTR(s.name, ' '))), ',', 3) AS stars\n" +
                    "FROM \n" +
                    "    movies t1 \n" +
                    "JOIN \n" +
                    "    ratings t2 ON t1.id = t2.movieId \n" +
                    "JOIN \n" +
                    "    genres_in_movies t3 ON t1.id = t3.movieId \n" +
                    "JOIN \n" +
                    "    genres g ON t3.genreId = g.id \n" +
                    "JOIN \n" +
                    "    stars_in_movies t4 ON t1.id = t4.movieId \n" +
                    "JOIN \n" +
                    "    stars s ON t4.starId = s.id \n" +
                    "GROUP BY \n" +
                    "    t1.id, \n" +
                    "    t2.rating\n" +
                    "ORDER BY \n" +
                    "    t2.rating DESC, \n" +
                    "    t1.title \n" +
                    "LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of r
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genre_names");
                String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("movie_rating", movie_rating);


                Statement stars_statement = conn.createStatement();

                String[] stars_list = movie_stars.split(",");

//                SELECT stars.id
//                FROM stars
//                JOIN stars_in_movies ON stars.id = stars_in_movies.starId
//                WHERE stars.name IN ('Alex Cox', 'Brendan Cleaves', 'Christian Marr', 'Christopher Cox')
//                AND stars_in_movies.movieId = 'tt0349853'
//                ORDER BY FIELD(name, 'name1', 'name2', 'name3');

                String stars_id_query = "SELECT stars.id FROM stars " +
                        "JOIN stars_in_movies ON stars.id = stars_in_movies.starId " +
                        "WHERE stars.name IN ('" +
                        stars_list[0] + "', '" + stars_list[1] + "', '" + stars_list[2] + "') " +
                        "AND stars_in_movies.movieId = '" + movie_id +
                        "' ORDER BY FIELD(name, '" + stars_list[0] + "', '" + stars_list[1] + "', '" + stars_list[2] + "');";

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

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
