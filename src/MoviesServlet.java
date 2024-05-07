import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.*;


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

        // Get a instance of current session on the request
        HttpSession session = request.getSession(false);

        String query = "";

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String search = "";
            Map<Integer, String> search_map = new HashMap<>();
            Map<Integer, Integer> limit_search_map = new HashMap<>();
            int search_count = 0;

            String having = "";
            boolean having_parameter = false;

            String default_sort = "ORDER BY t2.rating DESC, t1.title \n";


//                System.out.println(String.format("title: %s, year: %s, director: %s, star: %s\n genre: %s, char: %s\n, sort_method: %s",
//                        request.getParameter("title"), request.getParameter("year"), request.getParameter("director"),
//                        request.getParameter("star"), request.getParameter("genre"), request.getParameter("char"),
//                        request.getParameter("sort_method")));

            if (request.getParameter("title") != null || request.getParameter("year") != null ||
                    request.getParameter("director") != null || request.getParameter("star") != null) {

                session.setAttribute("char", null);
                session.setAttribute("genre", null);
            }

            if (request.getParameter("genre") != null || request.getParameter("char") != null) {

                session.setAttribute("title", null);
                session.setAttribute("year", null);
                session.setAttribute("director", null);
                session.setAttribute("star", null);

                if (request.getParameter("genre") != null) {
                    session.setAttribute("char", null);
                }

                if (request.getParameter("char") != null) {
                    session.setAttribute("genre", null);
                }
            }

            //search bar
            if (!(request.getParameter("title") == null) && !request.getParameter("title").isEmpty()) {
                search += "where title like ? ";
                search_count += 1;
                search_map.put(search_count, String.format("%%%s%%", request.getParameter("title")));

                session.setAttribute("title", request.getParameter("title"));
            } else {
                session.setAttribute("title", null);
            }


            if (!(request.getParameter("year") == null) && !request.getParameter("year").isEmpty()) {
                if (search_count == 0) {
                    search += "where year=? ";
                } else {
                    search += "and year=? ";
                }
                search_count += 1;
                search_map.put(search_count, String.format("%s", request.getParameter("year")));
                session.setAttribute("year", request.getParameter("year"));
            } else {
                session.setAttribute("year", null);
            }


            if (!(request.getParameter("director") == null) && !request.getParameter("director").isEmpty()) {
                if (search_count == 0) {
                    search += "where director like ? ";
                } else {
                    search += "and director like ? ";
                }
                search_count += 1;
                search_map.put(search_count, String.format("%%%s%%", request.getParameter("director")));
                session.setAttribute("director", request.getParameter("director"));
            } else {
                session.setAttribute("director", null);
            }


            if (!(request.getParameter("star") == null) && !request.getParameter("star").isEmpty()) {
                having += "having stars like ? ";
                having_parameter = true;
                search_count += 1;
                search_map.put(search_count, String.format("%%%s%%", request.getParameter("star")));
                session.setAttribute("star", request.getParameter("star"));
            } else {
                session.setAttribute("star", null);
            }

            //genre browsing section of main page
            if (request.getParameter("genre") != null && !request.getParameter("genre").isEmpty()) {
                if (!having_parameter) {
                    having += "having genre_names like ? ";
                } else {
                    having += "and genre_names like ? ";
                }
                search_count += 1;
                search_map.put(search_count, String.format("%%%s%%", request.getParameter("genre")));
                session.setAttribute("genre", request.getParameter("genre"));
            }

            //title browsing section
            if (request.getParameter("char") != null && !request.getParameter("char").isEmpty()) {
                if (search_count == 0) {
                    if (request.getParameter("char").equals("*")) {
                        search += "where title REGEXP '^\\\\W' ";
                    } else {
                        search += "where title like ? ";
                        search_count += 1;
                        search_map.put(search_count, String.format("%s%%", request.getParameter("char")));
                    }
                } else {
                    if (request.getParameter("char").equals("*")) {
                        search += "and title REGEXP '^\\\\W' ";
                    } else {
                        search += "and title like ? ";
                        search_count += 1;
                        search_map.put(search_count, String.format("%s%%", request.getParameter("char")));
                    }
                }

                session.setAttribute("char", request.getParameter("char"));
            }

            if (request.getParameter("sort_method") != null && !request.getParameter("sort_method").isEmpty()) {
                if (request.getParameter("sort_method").equals("RdTu")) {
                    default_sort = "ORDER BY t2.rating DESC, t1.title ASC ";
                } else if (request.getParameter("sort_method").equals("RdTd")) {
                    default_sort = "ORDER BY t2.rating DESC, t1.title DESC ";
                } else if (request.getParameter("sort_method").equals("RuTu")) {
                    default_sort = "ORDER BY t2.rating ASC, t1.title ASC ";
                } else if (request.getParameter("sort_method").equals("RuTd")) {
                    default_sort = "ORDER BY t2.rating ASC, t1.title DESC ";
                } else if (request.getParameter("sort_method").equals("TdRu")) {
                    default_sort = "ORDER BY t1.title DESC, t2.rating ASC ";
                } else if (request.getParameter("sort_method").equals("TdRd")) {
                    default_sort = "ORDER BY t1.title DESC, t2.rating DESC ";
                } else if (request.getParameter("sort_method").equals("TuRu")) {
                    default_sort = "ORDER BY t1.title ASC, t2.rating ASC ";
                } else if (request.getParameter("sort_method").equals("TuRd")) {
                    default_sort = "ORDER BY t1.title ASC, t2.rating DESC ";
                }

                session.setAttribute("sort_method", request.getParameter("sort_method"));
            }

            String LIMIT = "LIMIT 10 ";
            int number_movies = 10;
            if (request.getParameter("num_movies") != null && !request.getParameter("num_movies").isEmpty()) {
                LIMIT = "LIMIT ? ";
                search_count += 1;
                limit_search_map.put(search_count, Integer.parseInt(request.getParameter("num_movies")) + 1);
                session.setAttribute("num_movies", request.getParameter("num_movies"));
            }


            if (request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
                LIMIT += "OFFSET ?";
                search_count += 1;
                limit_search_map.put(search_count, number_movies * (Integer.parseInt(request.getParameter("page")) - 1));
                session.setAttribute("page", request.getParameter("page"));
            }

            // Declare our statement

//            SELECT t1.*, t2.rating, GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genre_names,
//            GROUP_CONCAT(DISTINCT s.name ORDER BY star_movie_count DESC, s.name) AS stars,
//            GROUP_CONCAT(DISTINCT s.id ORDER BY star_movie_count DESC, s.name) AS starnames
//            FROM movies t1 LEFT JOIN ratings t2 ON t1.id = t2.movieId INNER JOIN genres_in_movies t3 ON t1.id = t3.movieId
//            INNER JOIN genres g ON t3.genreId = g.id INNER JOIN stars_in_movies t4 ON t1.id = t4.movieId
//            INNER JOIN ( SELECT starId, COUNT(*) AS star_movie_count FROM stars_in_movies GROUP BY starId ) star_counts ON t4.starId = star_counts.starId
//            INNER JOIN stars s ON t4.starId = s.id GROUP BY t1.id, t2.rating ORDER BY t2.rating DESC, t1.title LIMIT 10;


            query = "SELECT t1.*, t2.rating, GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genre_names,\n" +
                    "GROUP_CONCAT(DISTINCT s.name ORDER BY star_movie_count DESC, s.name) AS stars,\n" +
                    "GROUP_CONCAT(DISTINCT s.id ORDER BY star_movie_count DESC, s.name) as star_names \n" +
                    "FROM movies t1\n" +
                    "LEFT JOIN ratings t2 ON t1.id = t2.movieId\n" +
                    "INNER JOIN genres_in_movies t3 ON t1.id = t3.movieId\n" +
                    "INNER JOIN genres g ON t3.genreId = g.id\n" +
                    "INNER JOIN stars_in_movies t4 ON t1.id = t4.movieId\n" +
                    "INNER JOIN ( SELECT starId, COUNT(*) AS star_movie_count FROM stars_in_movies GROUP BY starId ) star_counts ON t4.starId = star_counts.starId\n" +
                    "INNER JOIN stars s ON t4.starId = s.id \n" +
                    " " + search + " " +
                    "GROUP BY t1.id, t2.rating \n" +
                    " " + having + " " +
                    default_sort +
                    LIMIT + " ;";

            PreparedStatement statement = conn.prepareStatement(query);

            Set<Integer> keySet = search_map.keySet();
            ArrayList<Integer> listOfKeys = new ArrayList<>(keySet);
            Collections.sort(listOfKeys);
            for (Integer i: listOfKeys) {
                statement.setString(i, search_map.get(i));
            }

            Set<Integer> limitKeySet = limit_search_map.keySet();
            ArrayList<Integer> listOfLimitKeys = new ArrayList<>(limitKeySet);
            Collections.sort(listOfLimitKeys);
            for (Integer i: listOfLimitKeys) {
                statement.setInt(i, limit_search_map.get(i));
            }

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            boolean flag = false;
            // Iterate through each row of r
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");
                String movie_genres = rs.getString("genre_names");
                String movie_stars = rs.getString("stars");
                String movie_star_ids = rs.getString("star_names");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("stars_ids", movie_star_ids);
                if (!flag) {
                    flag = true;
                    jsonObject.addProperty("page", request.getParameter("page"));
                }

                jsonArray.add(jsonObject);

                request.getServletContext().log(query);
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
            jsonObject.addProperty("query!", query);
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
