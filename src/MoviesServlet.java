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
import java.sql.Statement;

import static java.lang.Math.min;
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

        if (request.getParameter("results") != null) {
            HttpSession redirect_session = request.getSession(false);
            String link = "?";

            List<String> parameters_array = new ArrayList<String>();
            if (redirect_session.getAttribute("title") != null) {
                parameters_array.add("title=" + redirect_session.getAttribute("title").toString());
            }
            if (redirect_session.getAttribute("year") != null) {
                parameters_array.add("year=" + redirect_session.getAttribute("year").toString());
            }
            if (redirect_session.getAttribute("director") != null) {
                parameters_array.add("director=" + redirect_session.getAttribute("director").toString());
            }
            if (redirect_session.getAttribute("star") != null) {
                parameters_array.add("star=" + redirect_session.getAttribute("star").toString());
            }
            if (redirect_session.getAttribute("genre") != null) {
                parameters_array.add("genre=" + redirect_session.getAttribute("genre").toString());
            }
            if (redirect_session.getAttribute("char") != null) {
                parameters_array.add("char=" + redirect_session.getAttribute("char").toString());
            }
            if (redirect_session.getAttribute("sort_method") != null) {
                parameters_array.add("sort_method=" + redirect_session.getAttribute("sort_method").toString());
            }
            if (redirect_session.getAttribute("num_movies") != null) {
                parameters_array.add("num_movies=" + redirect_session.getAttribute("num_movies").toString());
            }
            if (redirect_session.getAttribute("page") != null) {
                parameters_array.add("page=" + redirect_session.getAttribute("page").toString());
            }

            boolean flag = false;
            for (String parameter : parameters_array) {
                if (parameter != null && !parameter.isEmpty()) {
                    if (flag) {
                        link += "&" + parameter;
                    } else {
                        link += parameter;
                        flag = true;
                    }
                }
            }
            System.out.println(String.format("redirect link: %s + %s\n", request.getRequestURL().toString(), link));
            response.sendRedirect(request.getRequestURL().toString() + link);
        } else {

            response.setContentType("application/json"); // Response mime type

            // Output stream to STDOUT
            PrintWriter out = response.getWriter();

            // Get a instance of current session on the request
            HttpSession session = request.getSession(false);

            String query = "";

            // Get a connection from dataSource and let resource manager close the connection after usage.
            try (Connection conn = dataSource.getConnection()) {

                String search = "";
                boolean search_parameter = false;

                String having = "";
                boolean having_parameter = false;

                String default_sort = "ORDER BY t2.rating DESC, t1.title \n";
                int default_num = 10;
                String offset = "";

//                System.out.println(String.format("title: %s, year: %s, director: %s, star: %s\n genre: %s, char: %s\n, sort_method: %s",
//                        request.getParameter("title"), request.getParameter("year"), request.getParameter("director"),
//                        request.getParameter("star"), request.getParameter("genre"), request.getParameter("char"),
//                        request.getParameter("sort_method")));

                //search bar
                session.setAttribute("title", request.getParameter("title"));
                if (!(request.getParameter("title") == null) && !request.getParameter("title").isEmpty()) {
                    search += "where title like '%" + request.getParameter("title") + "%' ";
                    search_parameter = true;
                }

                session.setAttribute("year", request.getParameter("year"));
                if (!(request.getParameter("year") == null) && !request.getParameter("year").isEmpty()) {
                    if (!search_parameter) {
                        search += "where year=" + request.getParameter("year") + " ";
                        search_parameter = true;
                    } else {
                        search += "and year=" + request.getParameter("year") + " ";
                    }

                }

                session.setAttribute("director", request.getParameter("director"));
                if (!(request.getParameter("director") == null) && !request.getParameter("director").isEmpty()) {
                    if (!search_parameter) {
                        search += "where director like '%" + request.getParameter("director") + "%' ";
                        search_parameter = true;
                    } else {
                        search += "and director like '%" + request.getParameter("director") + "%' ";
                    }
                }

                session.setAttribute("star", request.getParameter("star"));
                if (!(request.getParameter("star") == null) && !request.getParameter("star").isEmpty()) {
                    having += "having stars like '%" + request.getParameter("star") + "%' ";
                    having_parameter = true;

                }

                //genre browsing section of main page
                session.setAttribute("genre", request.getParameter("genre"));
                if (request.getParameter("genre") != null && !request.getParameter("genre").isEmpty()) {
                    if (!having_parameter) {
                        having += "having genre_names like '%" + request.getParameter("genre") + "%' ";
                        having_parameter = true;
                    } else {
                        having += "and genre_names like '%" + request.getParameter("genre") + "%' ";
                    }
                }

                //title browsing section
                session.setAttribute("char", request.getParameter("char"));
                if (request.getParameter("char") != null && !request.getParameter("char").isEmpty()) {
                    if (!search_parameter) {
                        if (request.getParameter("char").equals("*")) {
                            search += "where title REGEXP '^\\\\W' ";
                        } else {
                            search += "where title like '" + request.getParameter("char") + "%' ";
                        }
                        search_parameter = true;
                    } else {
                        if (request.getParameter("char").equals("*")) {
                            search += "and title REGEXP '^\\\\W' ";
                        } else {
                            search += "and title like '" + request.getParameter("char") + "%' ";

                        }
                    }
                }

                session.setAttribute("sort_method", request.getParameter("sort_method"));
                if (request.getParameter("sort_method") != null && !request.getParameter("sort_method").isEmpty()) {

                    if (request.getParameter("sort_method").equals("RdTu")) {
                        default_sort = "ORDER BY t2.rating DESC, t1.title ASC \n";
                    } else if (request.getParameter("sort_method").equals("RdTd")) {
                        default_sort = "ORDER BY t2.rating DESC, t1.title DESC \n";
                    } else if (request.getParameter("sort_method").equals("RuTu")) {
                        default_sort = "ORDER BY t2.rating ASC, t1.title ASC \n";
                    } else if (request.getParameter("sort_method").equals("RuTd")) {
                        default_sort = "ORDER BY t2.rating ASC, t1.title DESC \n";
                    } else if (request.getParameter("sort_method").equals("TdRu")) {
                        default_sort = "ORDER BY t1.title DESC, t2.rating ASC \n";
                    } else if (request.getParameter("sort_method").equals("TdRd")) {
                        default_sort = "ORDER BY t1.title DESC, t2.rating DESC \n";
                    } else if (request.getParameter("sort_method").equals("TuRu")) {
                        default_sort = "ORDER BY t1.title ASC, t2.rating ASC \n";
                    } else if (request.getParameter("sort_method").equals("TuRd")) {
                        default_sort = "ORDER BY t1.title ASC, t2.rating DESC \n";
                    }
                }

                session.setAttribute("num_movies", request.getParameter("num_movies"));
                if (request.getParameter("num_movies") != null && !request.getParameter("num_movies").isEmpty()) {
                    default_num = Integer.parseInt(request.getParameter("num_movies"));
                }

                session.setAttribute("page", request.getParameter("page"));
                if (request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
                    offset += "OFFSET ";
                    int offset_num = default_num * (Integer.parseInt(request.getParameter("page")) - 1);
                    offset += String.valueOf(offset_num);
                }

//            System.out.println(String.format("search: %s", search));

                // Declare our statement
                Statement statement = conn.createStatement();

//            SELECT t1.*, t2.rating, GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genre_names,
//            GROUP_CONCAT(DISTINCT s.name ORDER BY star_movie_count DESC, s.name) AS stars
//            FROM movies t1 LEFT JOIN ratings t2 ON t1.id = t2.movieId INNER JOIN genres_in_movies t3 ON t1.id = t3.movieId
//            INNER JOIN genres g ON t3.genreId = g.id INNER JOIN stars_in_movies t4 ON t1.id = t4.movieId
//            INNER JOIN ( SELECT starId, COUNT(*) AS star_movie_count FROM stars_in_movies GROUP BY starId ) star_counts ON t4.starId = star_counts.starId
//            INNER JOIN stars s ON t4.starId = s.id GROUP BY t1.id, t2.rating ORDER BY t2.rating DESC, t1.title LIMIT 100;


                query = "SELECT t1.*, t2.rating, GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genre_names,\n" +
                        "GROUP_CONCAT(DISTINCT s.name ORDER BY star_movie_count DESC, s.name) AS stars\n" +
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
                        "LIMIT "+ String.valueOf(default_num + 1) + " " + offset + ";";

                // Perform the query
                ResultSet rs = statement.executeQuery(query);

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

                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_year", movie_year);
                    jsonObject.addProperty("movie_director", movie_director);
                    jsonObject.addProperty("movie_rating", movie_rating);
                    jsonObject.addProperty("movie_genres", movie_genres);
                    jsonObject.addProperty("movie_stars", movie_stars);
                    if (!flag) {
                        flag = true;
                        jsonObject.addProperty("page", request.getParameter("page"));
                    }


                    Statement stars_statement = conn.createStatement();

                    String[] stars_list = movie_stars.split(",");

//                SELECT stars.id
//                FROM stars
//                JOIN stars_in_movies ON stars.id = stars_in_movies.starId
//                WHERE stars.name IN ('Alex Cox', 'Brendan Cleaves', 'Christian Marr', 'Christopher Cox')
//                AND stars_in_movies.movieId = 'tt0349853'
//                ORDER BY FIELD(name, 'name1', 'name2', 'name3');

                    String stars_list_string = "";
                    for (int i = 0; i < min(stars_list.length, 3); i++) {
                        stars_list_string += stars_list[i];
                        if (i < min(stars_list.length, 3) - 1) {
                            stars_list_string += "', '";
                        } else {
                            stars_list_string += "') ";
                        }
                    }
                    String stars_id_query = "SELECT stars.id FROM stars " +
                            "JOIN stars_in_movies ON stars.id = stars_in_movies.starId " +
                            "WHERE stars.name IN ('" +
                            stars_list_string +
                            "AND stars_in_movies.movieId = '" + movie_id +
                            "' ORDER BY FIELD(name, '" + stars_list_string + ";";

                    ResultSet stars_id_set = stars_statement.executeQuery(stars_id_query);

                    StringBuilder stars_ids = new StringBuilder();
                    while (stars_id_set.next()) {
                        stars_ids.append(stars_id_set.getString("id")).append(",");
                    }
                    jsonObject.addProperty("stars_ids", stars_ids.toString());
                    jsonArray.add(jsonObject);

                    stars_id_set.close();
                    stars_statement.close();

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

            // Always remember to close db connection after usage. Here it's done by try-with-resources
        }
    }
}
