import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
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

        Map<String, List<Object>> previousItems = (Map<String, List<Object>>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<>();
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");

        JsonArray previousItemsJsonArray = new JsonArray();
        for (Map.Entry<String, List<Object>> entry : previousItems.entrySet()) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("movie_id", entry.getKey());
            jsonObject.addProperty("movie_title", entry.getValue().get(0).toString());
            jsonObject.addProperty("count", entry.getValue().get(1).toString());
            previousItemsJsonArray.add(jsonObject);
        }

        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String item = request.getParameter("movie_id");
        String item2 = request.getParameter("movie_title");

        System.out.printf("item: %s; item2: %s%n", item, item2);
        
        HttpSession session = request.getSession();

        Map<String, List<Object>> previousItems = (Map<String, List<Object>>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<>();
            List<Object> hashMapList = new ArrayList<>(Arrays.asList(item2, 1));
            previousItems.put(item, hashMapList);

            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                if (previousItems.containsKey(item)) {
                    Integer prev_num = (Integer) previousItems.get(item).get(1);
                    previousItems.get(item).set(1, prev_num + 1);
                } else {
                    List<Object> hashMapList = new ArrayList<>(Arrays.asList(item2, 1));
                    previousItems.put(item, hashMapList);
                }
            }
//            previousItems.merge(item+","+item2, 1, Integer::sum);
        }

        JsonArray previousItemsJsonArray = new JsonArray();
        for (Map.Entry<String, List<Object>> entry : previousItems.entrySet()) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("movie_id", entry.getKey());
            jsonObject.addProperty("movie_title", entry.getValue().get(0).toString());
            jsonObject.addProperty("count", entry.getValue().get(1).toString());
            previousItemsJsonArray.add(jsonObject);
        }
    }


    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();

        String movie_id = request.getParameter("movie_id");

        System.out.printf("movie_id: %s;\n", movie_id);

        try {
            Map<String, List<Object>> previousItems = (Map<String, List<Object>>) session.getAttribute("previousItems");
            if (request.getParameter("count") == null && movie_id != null) {
                if (previousItems != null) {
                    Iterator<Map.Entry<String, List<Object>>> iterator = previousItems.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, List<Object>> entry = iterator.next();
                        if (entry.getKey().equals(movie_id)) {
                            iterator.remove();
                        }
                    }
                }
            } else if (request.getParameter("count") != null && movie_id != null) {
                if (previousItems != null) {
                    Iterator<Map.Entry<String, List<Object>>> iterator = previousItems.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, List<Object>> entry = iterator.next();
                        if (entry.getKey().equals(movie_id)) {
                            Integer prev_num = (Integer) previousItems.get(movie_id).get(1);
                            if (prev_num - 1 == 0) {
                                iterator.remove();
                            } else {
                                previousItems.get(movie_id).set(1, prev_num - 1);
                            }
                        }
                    }
                }
            }
            JsonObject responseJsonObject = new JsonObject();
            session.setAttribute("previousItems", previousItems);

            JsonArray previousItemsJsonArray = new JsonArray();
            for (Map.Entry<String, List<Object>> entry : previousItems.entrySet()) {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("movie_id", entry.getKey());
                jsonObject.addProperty("movie_title", entry.getValue().get(0).toString());
                jsonObject.addProperty("count", entry.getValue().get(1).toString());
                previousItemsJsonArray.add(jsonObject);
            }
            responseJsonObject.add("previousItems", previousItemsJsonArray);

            // write all the data into the jsonObject
            response.getWriter().write(responseJsonObject.toString());

        } catch (Exception e) {
            System.out.print("my error that I made error: ");
            System.out.println(e.getMessage());
        }
    }
}