import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class StarDomParse {

    Document dom;
    String maxId;

    public Map<String, ArrayList<Object>> runStarDomParse() {

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each employee element and create a Employee object

        return parseDocument();
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            System.out.println("system running...");
            dom = documentBuilder.parse("stanford-movies/actors63.xml");
            System.out.println("System passed documentbuilder...");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private Map<String, ArrayList<Object>> parseDocument() {
        Map<String, ArrayList<Object>> actors = new HashMap<>();
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

                statement.close();
                rs.close();
            } else {
                System.out.println("no connection to database");
                return actors;
            }

            // get the document root Element
            Element documentElement = dom.getDocumentElement();

            // get a nodelist of employee Elements, parse each into Employee object
            NodeList nodeList = documentElement.getElementsByTagName("actor");
            System.out.println("going through actors...");

            int counter = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (i % 500 == 0) {
                    System.out.println("i: " + i);
                }
                // get the star element
                Element element = (Element) nodeList.item(i);

                // get the Employee object
                Star star = parseStar(element);

                if (star.getName() != null) {
                    counter++;
                    ArrayList<Object> actor_info = new ArrayList<Object>(Arrays.asList(star.getId(), star.getBirthYear()));
                    actors.put(star.getName(), actor_info);
//                    if (star.getBirthYear() == 0) {
//                        newStars.write(star.getId() + "," + star.getName() + "\n");
//                    } else {
//                        newStars.write(star + "\n");
//                    }
                    continue;
                }
            }

            System.out.println("actors63 counter: " + counter + "\n");

        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
        }
        return actors;
    }

    private Star parseStar(Element element) {

        String id = maxId.substring(0, 2) + (Integer.parseInt(maxId.substring(2)) + 1);
        maxId = id;
        String name = getTextValue(element, "stagename");
        int age = getIntValue(element, "dob");

        if (name == null) {
            maxId = maxId.substring(0, 2) + (Integer.parseInt(maxId.substring(2)) - 1);
        }

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
        StarDomParse starDomParse = new StarDomParse();

        // call run example
        Map<String, ArrayList<Object>> actors = starDomParse.runStarDomParse();
    }
}
