import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.util.*;

// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "ResultsServlet", urlPatterns = "/api/results")
public class ResultsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession redirect_session = request.getSession(false);
        String link = "?";

        List<String> parameters_array = new ArrayList<>();
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
        String[] url_array = request.getRequestURL().toString().split("/");
        String url_trimmed = String.join("/", Arrays.copyOfRange(url_array, 0, url_array.length - 2)) + "/movielist.html";
        System.out.printf("redirect link: %s + %s%n", url_trimmed, link);

        response.getWriter().write(url_trimmed + link);
    }
}