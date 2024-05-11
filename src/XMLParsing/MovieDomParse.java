package XMLParsing;

import com.google.gson.JsonObject;
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


public class MovieDomParse {

//    List<Movie> movies = new ArrayList<>();
    Document dom;
    int maxId;

    Map<String, String> categories = new HashMap<>();
    Set<String> movieIds = new HashSet<>();

    public Set<String> runMovieDomParse() {
        categories.put("susp", "Thriller");
        categories.put("cnr", "Cops and Robbers");
        categories.put("dram", "Drama");
        categories.put("west", "Western");
        categories.put("myst", "Mystery");
        categories.put("s.f.", "Sci-Fi");
        categories.put("advt", "Adventure");
        categories.put("horr", "Horror");
        categories.put("romt", "Romance");
        categories.put("comd", "Comedy");
        categories.put("musc", "Musical");
        categories.put("docu", "Documentary");
        categories.put("porn", "Pornography");
        categories.put("noir", "Black");
        categories.put("biop", "Biographical Picture");
        categories.put("tv", "TV Show");
        categories.put("tvs", "TV Series");
        categories.put("tvm", "TV Miniseries");

        categories.put("actn", "Action");
        categories.put("camp now", "Camp");
        categories.put("disa", "Disaster");
        categories.put("epic", "Epic");
        categories.put("cart", "Cartoon");
        categories.put("faml", "Family");
        categories.put("surl", "Surreal");
        categories.put("avga", "Avant Garde");
        categories.put("hist", "History");
        categories.put("scfi", "Sci-Fi");

        categories.put("fant", "Fantasy");
        categories.put("cnrb", "Cops and Robbers");
        categories.put("muscl", "Musical");
        categories.put("dist", "Disaster");
        categories.put("scif", "Sci-Fi");
        categories.put("camp", "Camp");
        categories.put("surr", "Surreal");

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each employee element and create an Employee object
        parseDocument();

        return movieIds;
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            System.out.println("system running...");
            dom = documentBuilder.parse("stanford-movies/mains243.xml");
            System.out.println("System passed documentbuilder...");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
            System.out.println("why is it not running?");
        }
    }

    private void parseDocument() {
        try {
            //opening files
            FileWriter newMoviesWriter = new FileWriter("src/XMLParsing/mains_movies.txt", false);
            FileWriter genresInMoviesWriter = new FileWriter("src/XMLParsing/mains_genres_in_movies.txt", false);
            FileWriter genresWriter = new FileWriter("src/XMLParsing/mains_genres.txt", false);
            FileWriter errorLogWriter = new FileWriter("src/XMLParsing/mains_error_log.txt", false);

            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
                    Parameters.username, Parameters.password);

            Map<String, Integer> genres = new HashMap<>();
            Set<String> movieIdSet = new HashSet<>();
            if (connection != null) {
//                JsonObject jsonObject = new JsonObject();

                String query = "select * from genres;";
                Statement statement = connection.createStatement();

                ResultSet rs = statement.executeQuery(query);

                while (rs.next()) {
                    String genreId = rs.getString("id");
                    String genreName = rs.getString("name");

                    genres.put(genreName, Integer.parseInt(genreId));
                }

                String maxIdQuery = "select max(id) from genres;";
                Statement maxStatement = connection.createStatement();
                ResultSet maxIdRs = maxStatement.executeQuery(maxIdQuery);
                maxIdRs.next();
                maxId = Integer.parseInt(maxIdRs.getString("max(id)"));

                statement.close();
                rs.close();
                maxStatement.close();
                maxIdRs.close();
            } else {
                System.out.println("No connection");
                return;
            }

            // get the document root Element
            Element documentElement = dom.getDocumentElement();

            // get a nodelist of employee Elements, parse each into Employee object
            NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
            System.out.println("system going through directorfilms...");
            int counter = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {

                // get the employee element
                Element element = (Element) nodeList.item(i);

                String directorName = getTextValue(element, "dirname");

                //error if director name is null
                if (directorName == null) {
                    errorLogWriter.write("director null\n");
                    continue;
                }

                NodeList filmsList = element.getElementsByTagName("films");

                for (int j = 0; j < filmsList.getLength(); j++) {
                    NodeList filmList = ((Element) filmsList.item(j)).getElementsByTagName("film");

                    for (int k = 0; k < filmList.getLength(); k++) {
                        Element film = (Element) filmList.item(k);

                        Movie movie = parseMovie(film, directorName);

                        //error if id is already in database
                        if (movieIdSet.contains(movie.getId())) {
                            errorLogWriter.write("duplicate id: " + movie + "," + movie.getGenre() + "\n");
                            continue;
                        }

                        //error if title, id, or year are null
                        if (movie.getTitle() == null || movie.getId() == null || movie.getYear() == 0) {
                            //error writing
                            errorLogWriter.write("Null essential entry: " + movie + "," + movie.getGenre() + "\n");
                            continue;
                        }

                        //iterate through film's genres to update genres and genres_in_movies
                        String writerString = "";
                        boolean flag = false;
                        for (String outerS: movie.getGenre()) {
                            String[] stringSplit = outerS.split(" ");

                            for (String s: stringSplit) {
                                if (!genres.containsKey(categories.get(s.toLowerCase().replaceAll("\\s", "")))) {
                                    if (categories.get(s.toLowerCase()) != null) {
                                        maxId++;
                                        genresWriter.write(categories.get(s.toLowerCase()) + "," + maxId + "\n");
                                        genres.put(categories.get(s.toLowerCase()), maxId);
                                    } else {
                                        errorLogWriter.write("Invalid genre: " + movie + "," + movie.getGenre() + "\n");
                                        flag = true;
                                        break;
                                    }
                                }
                                writerString += genres.get(categories.get(s.toLowerCase())) + "," + movie.getId() + "\n";
                            }

                            if (flag) {break; }
                        }

                        if (!flag) {
                            counter++;
                            movieIdSet.add(movie.getId());
                            newMoviesWriter.write(movie + "\n");
                            genresInMoviesWriter.write(writerString);
                            movieIds.add(movie.getId());
                        }
                    }
                }
            }
            System.out.println("mains243 counter: " + counter + "\n");
            newMoviesWriter.close();
            genresInMoviesWriter.close();
            genresWriter.close();
            errorLogWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private Movie parseMovie(Element element, String director) {

        String id = getTextValue(element, "fid");
        String title = getTextValue(element, "t");
        int year = getIntValue(element, "year");
        ArrayList<String> genre = getArrayValue(element, "cat");
        // create a new Employee with the value read from the xml nodes
        return new Movie(id, title, year, director, genre);
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e. for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
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

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        try {
            String stringOfInt = getTextValue(ele, tagName);
            if (stringOfInt != null) {
                return Integer.parseInt(stringOfInt);
            } else {
                return 0;
            }
        } catch (Exception error) {
            return 0;
        }
    }

    private ArrayList<String> getArrayValue(Element element, String tagName) {
        ArrayList<String> returnArray = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                try {
                    String value = nodeList.item(i).getFirstChild().getNodeValue();
                    if (value != null) {
                        returnArray.add(value);
                    }
                } catch (Exception e) {
//                    System.out.println(e.getClass().getName());
                }
            }
        }
        return returnArray;
    }


    public static void main(String[] args) {
        // create an instance
        MovieDomParse movieDomParse = new MovieDomParse();

        // call run example
        Set<String> movieIds = movieDomParse.runMovieDomParse();
    }
}
