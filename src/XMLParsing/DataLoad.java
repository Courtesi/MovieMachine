package XMLParsing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class DataLoad {
    public static void main(String[] args) {
        // create an instance
        MovieDomParse movieDomParse = new MovieDomParse();
        Set<String> movieIds = movieDomParse.runMovieDomParse();

        StarDomParse starDomParse = new StarDomParse();
        Map<String, ArrayList<Object>> actors = starDomParse.runStarDomParse();

        CastDomParse castDomParse = new CastDomParse();
        castDomParse.runCastDomParse(movieIds, actors);

        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
                    Parameters.username, Parameters.password);

            if (connection != null) {
                Statement statement = connection.createStatement();

                String query = "";

            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


    }
}
