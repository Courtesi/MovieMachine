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
import java.sql.*;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/_dashboard/api/employee_login")
public class EmployeeLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slave");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        System.out.println("gRecaptcha: " + gRecaptchaResponse);
//
//        try {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//        } catch (Exception e) {
//            request.getServletContext().log("Login failed");
//            JsonObject responseJsonObject = new JsonObject();
//            responseJsonObject.addProperty("status", "fail");
//            responseJsonObject.addProperty("message", e.getMessage());
//            response.getWriter().write(responseJsonObject.toString());
//            response.setStatus(200);
//            return;
//        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = "select * from employees where email = ?;";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();
            if (rs.isBeforeFirst()) {
                rs.next();

                String encryptedPassword = rs.getString("password");
                boolean success = encryptedPassword.equals(password);
//                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                if (success) {
                    // Login success:

                    // set this user into the session
                    request.getSession().setAttribute("user", new User(username));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Success");
                } else {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");
                    // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                    responseJsonObject.addProperty("message", "Incorrect password");
                }
            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "User " + username + " doesn't exist");
            }
            response.getWriter().write(responseJsonObject.toString());
            rs.close();
            statement.close();
            response.setStatus(200);

        } catch (Exception e) {
            e.printStackTrace();

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("message", e.getMessage());
            jsonObject.addProperty("exception", e.getClass().getName());
//            jsonObject.addProperty("captcha", gRecaptchaResponse);
            response.getWriter().write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            response.getWriter().close();
        }
    }
}
