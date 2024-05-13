import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "AddMovie", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_title = request.getParameter("movie_title");
        String movie_year = request.getParameter("movie_year");
        String movie_director = request.getParameter("movie_director");
        String star_name = request.getParameter("star_name");
        String birth_year = request.getParameter("birth_year");
        String genre_name = request.getParameter("genre_name");

        if (movie_title == null || movie_title.isEmpty() ||
                movie_year == null || movie_year.isEmpty() ||
                movie_director == null || movie_director.isEmpty()) {
            JsonObject json = new JsonObject();
            json.addProperty("status", "fail");
            json.addProperty("message", "Invalid movie entry");
            response.getWriter().write(json.toString());
            response.setStatus(200);
            return;
        }

        if (!movie_year.matches("-?(0|[1-9]\\d*)")) {
            JsonObject json = new JsonObject();
            json.addProperty("status", "fail");
            json.addProperty("message", "Movie year entered is not a valid year");
            response.getWriter().write(json.toString());
            response.setStatus(200);
            return;
        }

        if (!((birth_year == null) || (birth_year.isEmpty())) && (!birth_year.matches("-?(0|[1-9]\\d*)"))) {
            JsonObject json = new JsonObject();
            json.addProperty("status", "fail");
            json.addProperty("message", "Birth year entered is not a valid year");
            response.getWriter().write(json.toString());
            response.setStatus(200);
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "CALL moviedb.add_movie(?, ?, ?, ?, ?, ?);";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, movie_title);
            ps.setString(2, movie_year);
            ps.setString(3, movie_director);
            ps.setString(4, star_name);

            if (birth_year == null || birth_year.isEmpty()) {
                ps.setString(5, String.valueOf(0));
            } else {
                ps.setString(5, birth_year);
            }

            ps.setString(6, genre_name);

            ResultSet rs = ps.executeQuery();
            rs.next();
            String result = rs.getString("RESULT");

            if (result.equals("Movie already exists")) {
                JsonObject json = new JsonObject();
                json.addProperty("status", "fail");
                json.addProperty("message", "Movie already exists");
                response.getWriter().write(json.toString());
                response.setStatus(200);
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("status", "success");
            json.addProperty("message", "<a>" + result + "</a>");
            response.getWriter().write(json.toString());
            response.setStatus(200);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject returnObject = new JsonObject();
            returnObject.addProperty("status", "fail");
            returnObject.addProperty("message", e.getMessage() + ", " + e.getClass().getName());
            response.getWriter().write(returnObject.toString());
            response.setStatus(500);
        } finally {
            response.getWriter().close();
        }
    }
}
