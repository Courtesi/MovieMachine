import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        Map<String, Integer> previousItems = (Map<String, Integer>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, Integer>();
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");
        JsonArray previousItemsJsonArray = new JsonArray();


        for (Map.Entry<String, Integer> entry : previousItems.entrySet()) {
            JsonObject jsonObject = new JsonObject();

            String[] items = entry.getKey().split(",");
            jsonObject.addProperty("movie_id", items[0]);
            jsonObject.addProperty("movie_title", items[1]);
            jsonObject.addProperty("count", entry.getValue().toString());
            previousItemsJsonArray.add(jsonObject);
        }

        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("movieId");
        String item2 = request.getParameter("movieTitle");

        System.out.println(String.format("item: %s; item2: %s", item, item2));


        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        Map<String, Integer> previousItems = (Map<String, Integer>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, Integer>();
            previousItems.merge(item+","+item2, 1, Integer::sum);
            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                previousItems.merge(item+","+item2, 1, Integer::sum);
            }
        }

        JsonArray previousItemsJsonArray = new JsonArray();
        for (Map.Entry<String, Integer> entry : previousItems.entrySet()) {
            JsonObject jsonObject = new JsonObject();

            String[] items = entry.getKey().split(",");
            jsonObject.addProperty("movie_id", items[0]);
            jsonObject.addProperty("movie_title", items[1]);
            jsonObject.addProperty("count", entry.getValue().toString());
            previousItemsJsonArray.add(jsonObject);
        }
    }


    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));

//        String data = br.readLine();

//        System.out.println(String.format("data: %s", data));

        String data = request.getParameter("movie_id");

        try {
            if (request.getParameter("count") == null && data != null) {

                JsonObject responseJsonObject = new JsonObject();
                Map<String, Integer> previousItems = (Map<String, Integer>) session.getAttribute("previousItems");


                if (previousItems != null) {
                    Iterator<Map.Entry<String, Integer>> iterator = previousItems.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Integer> entry = iterator.next();
                        if (entry.getKey().toUpperCase().contains(data.toUpperCase())) {
                            iterator.remove();
                        }
                    }
                }

                session.setAttribute("previousItems", previousItems);

                JsonArray previousItemsJsonArray = new JsonArray();
                for (Map.Entry<String, Integer> entry : previousItems.entrySet()) {
                    JsonObject jsonObject = new JsonObject();

                    String[] items = entry.getKey().split(",");
                    jsonObject.addProperty("movie_id", items[0]);
                    jsonObject.addProperty("movie_title", items[1]);
                    jsonObject.addProperty("count", entry.getValue().toString());
                    previousItemsJsonArray.add(jsonObject);
                }
                responseJsonObject.add("previousItems", previousItemsJsonArray);

                // write all the data into the jsonObject
                response.getWriter().write(responseJsonObject.toString());
            }
        } catch (Exception e) {
            System.out.print("my error that I made error: ");
            System.out.println(e.getMessage());
        }

    }
}