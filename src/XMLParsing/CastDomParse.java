package XMLParsing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;



public class CastDomParse {

    Document dom;
    String maxId;
    String securePath;

    public void runCastDomParse(Set<String> movieIds, Map<String, ArrayList<Object>> actors) {


        // parse the xml file and get the dom object
        parseXmlFile();

        // get each employee element and create a Employee object
        parseDocument(movieIds, actors);
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            System.out.println("system running...");
            dom = documentBuilder.parse("stanford-movies/casts124.xml");
            System.out.println("System passed documentbuilder...");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument(Set<String> movieIds, Map<String, ArrayList<Object>> actors) {
        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
                    Parameters.username, Parameters.password);

            if (connection != null) {
                String query = "select max(id) from stars;";
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(query);

                rs.next();
                maxId = rs.getString("max(id)");

                String secureQuery = "show variables like 'secure_file_priv';";
                Statement secureStatement = connection.createStatement();
                ResultSet secureRs = secureStatement.executeQuery(secureQuery);
                secureRs.next();
                securePath = secureRs.getString("value");
                System.out.println("kajsghdashjkgd: " + securePath);

                statement.close();
                rs.close();
                secureStatement.close();
                secureRs.close();
            } else {
                System.out.println("no connection to database");
                return;
            }

            FileWriter newStars = new FileWriter(securePath + "casts_stars.txt", false);
            FileWriter newStarsInMovies = new FileWriter(securePath + "casts_stars_in_movies.txt", false);
            FileWriter errorLog = new FileWriter("casts_error_log.txt", false);

            // get the document root Element
            Element documentElement = dom.getDocumentElement();

            // get a nodelist of employee Elements, parse each into Employee object
            int counter = 0;
            NodeList nodeList = documentElement.getElementsByTagName("dirfilms");
            System.out.println("going through director films in cast...");
            Set<String> actorRepeats = new HashSet<>();
            Set<String> actorss = new HashSet<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                // get the star element
                Element element = (Element) nodeList.item(i);

                NodeList filmcs = element.getElementsByTagName("filmc");

                for (int j = 0; j < filmcs.getLength(); j++) {
                    NodeList mtags = ((Element) filmcs.item(j)).getElementsByTagName("m");

                    for (int k = 0; k < mtags.getLength(); k++) {
                        Element mtag = (Element) mtags.item(k);

                        String starName = getTextValue(mtag, "a");
                        String movieId = getTextValue(mtag, "f");

                        if (starName != null && movieId != null && !starName.replaceAll("\\s", "").equalsIgnoreCase("sa") && actors.containsKey(starName) && movieIds.contains(movieId)) {
                            counter++;
                            if (!actorss.contains(actors.get(starName).get(0) + "," + movieId)) {
                                newStarsInMovies.write(actors.get(starName).get(0) + "," + movieId + "\n");
                                actorss.add(actors.get(starName).get(0) + "," + movieId);
                            }
                            if (!actorRepeats.contains((String)actors.get(starName).get(0))) {
                                newStars.write(actors.get(starName).get(0) + "," + starName + "," + actors.get(starName).get(1) + "\n");
                            }

                            actorRepeats.add((String)actors.get(starName).get(0));
                        } else {
                            errorLog.write("invalid entry: " + starName + "\n");
                        }
                    }
                }
            }

            System.out.println("casts124 counter: " + counter);
            newStars.close();
            newStarsInMovies.close();
            errorLog.close();

        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
        }

    }

    private Star parseStar(Element element) {

        String id = maxId.substring(0, 2) + (Integer.parseInt(maxId.substring(2)) + 1);
        maxId = id;
        String name = getTextValue(element, "a");
        int age = 0;

        return new Star(id, name, age);
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            try {
                textVal = nodeList.item(0).getFirstChild().getNodeValue();
            } catch (Exception e) {
                return null;
            }
        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName) {
        String temp = getTextValue(ele, tagName);
        if (temp == null) {
            return 0;
        }

        try {
            return Integer.parseInt(temp);
        } catch(Exception e) {
            return 0;
        }
    }


    public static void main(String[] args) {
        // create an instance
//        MovieDomParse movieDomParse = new MovieDomParse();
//        Set<String> movieIds = movieDomParse.runMovieDomParse();
//
//        StarDomParse starDomParse = new StarDomParse();
//        Map<String, ArrayList<Object>> actors = starDomParse.runStarDomParse();
//
//        CastDomParse castDomParse = new CastDomParse();
//        castDomParse.runCastDomParse(movieIds, actors);
    }
}
