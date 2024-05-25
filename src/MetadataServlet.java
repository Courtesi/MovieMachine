import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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
import java.util.*;

@WebServlet(name = "MetadataServlet", urlPatterns = "/_dashboard/api/metadata")
public class MetadataServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String tableQuery = "show tables;";

            Statement tableStatement = conn.createStatement();
            ResultSet rs = tableStatement.executeQuery(tableQuery);

            Statement perTableStatement = conn.createStatement();

            JsonArray returnArray = new JsonArray();
            while (rs.next()) {
                JsonObject json = new JsonObject();
                String tableName = rs.getString("Tables_in_moviedb");
                json.addProperty("TableName", tableName);

                String tableMetadataQuery = String.format("describe %s;", tableName);
                ResultSet metadataRs = perTableStatement.executeQuery(tableMetadataQuery);

                StringBuilder fields = new StringBuilder();
                StringBuilder types = new StringBuilder();
                while (metadataRs.next()) {
                    fields.append(metadataRs.getString("Field")).append(",");
                    types.append(metadataRs.getString("Type")).append(",");
                }

                json.addProperty("Fields", fields.toString());
                json.addProperty("Types", types.toString());

                metadataRs.close();

                returnArray.add(json);
            }

            tableStatement.close();
            rs.close();
            perTableStatement.close();

            out.write(returnArray.toString());
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
    }
}
