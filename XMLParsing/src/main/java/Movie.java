
import java.util.ArrayList;

public class Movie {

    private final String id;

    private final String title;

    private final int year;

    private final String director;

    final ArrayList<String> genre;


    public Movie(String id, String title, int year, String director, ArrayList<String> genre) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genre = genre;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public ArrayList<String> getGenre() {return genre; }

    public String toString() {
        return getId() + "," + getTitle() + "," + getYear() + "," + getDirector();

//        return "ID:" + getId() + ", " +
//                "Title:" + getTitle() + ", " +
//                "Year:" + getYear() + ", " +
//                "Director:" + getDirector() + ", " +
//                "Genre:" + getGenre();
    }
}
