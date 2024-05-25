import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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

@WebServlet("/hero-suggestion")
public class SearchServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = dataSource.getConnection()) {
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            // search on superheroes and add the results to JSON Array
            // this example only does a substring match
            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
            String searchQuery = "select id, title from movies where match (title) against (? IN BOOLEAN MODE);";

            String tokenizedQuery = tokenizeQuery(query);

            PreparedStatement statement = conn.prepareStatement(searchQuery);
            statement.setString(1, tokenizedQuery);

            ResultSet rs = statement.executeQuery();

            int counter = 0;
            while (rs.next()) {
                if (counter >= 10) {
                    break;
                }

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                counter++;

                jsonArray.add(generateJsonObject(movie_id, movie_title));
            }

            statement.close();
            rs.close();

            System.out.println(jsonArray);

            response.getWriter().write(jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error_message", e.getMessage());
            jsonObject.addProperty("exception", e.getClass().getName());
            response.getWriter().write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movie_id, String movie_title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movie_title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movie_id", movie_id);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }

    public static String tokenizeQuery(String input) {
        // Split the input string into words
        String[] words = input.split(" ");

        // StringBuilder to hold the resulting string
        StringBuilder result = new StringBuilder();

        // Iterate through each word
        for (String word : words) {
            // Add + before the word and * after the word
            result.append("+").append(word).append("*").append(" ");
        }

        // Convert StringBuilder to String and trim the trailing space
        return result.toString().trim();
    }
}
