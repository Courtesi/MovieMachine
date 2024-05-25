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
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "AddStar", urlPatterns = "/_dashboard/api/add-star")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String starName = request.getParameter("star_name");
        String birthYear = request.getParameter("birth_year");

        if (starName == null || starName.isEmpty()) {
            JsonObject json = new JsonObject();
            json.addProperty("status", "fail");
            json.addProperty("message", "Invalid star name");
            response.getWriter().write(json.toString());
            response.setStatus(200);
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "select max(id) from stars;";
            Statement statement = conn.createStatement();
            ResultSet max_id = statement.executeQuery(query);

            max_id.next();
            String maxId = max_id.getString("max(id)");

            String newId = maxId.substring(0, 2) + (Integer.parseInt(maxId.substring(2)) + 1);

            String insertQuery;
            if (birthYear == null || birthYear.isEmpty()) {
                insertQuery = String.format("INSERT INTO stars (id, name) values ('%s', '%s');", newId, starName);
            } else {
                if (!birthYear.matches("-?(0|[1-9]\\d*)")) {
                    JsonObject json = new JsonObject();
                    json.addProperty("status", "fail");
                    json.addProperty("message", "Invalid birth year: not an integer");
                    response.getWriter().write(json.toString());
                    response.setStatus(200);
                    return;
                }
                insertQuery = String.format("INSERT INTO stars (id, name, birthYear) values ('%s', '%s', '%s');", newId, starName, birthYear);
            }

            statement.executeUpdate(insertQuery);

            JsonObject returnObject = new JsonObject();
            returnObject.addProperty("status", "success");
            returnObject.addProperty("message", "<a>Success! Star ID: " + newId + "</a>");
            response.getWriter().write(returnObject.toString());
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
