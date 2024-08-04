package booking.entites;

import java.util.ArrayList;
import java.util.List;

import booking.CborSerializable;

public class InitShowList implements CborSerializable {
    List<Show> shows;

    public InitShowList(){
        this.shows=new ArrayList<>();
        this.shows.add(new Show(1, 1, "Youth in Revolt", 50, 40));
        this.shows.add(new Show(2, 1, "Leap Year", 55, 30));
        this.shows.add(new Show(3, 1, "Remember Me", 60, 55));
        this.shows.add(new Show(4, 2, "Fireproof", 65, 65));
        this.shows.add(new Show(5, 2, "Beginners", 55, 50));
        this.shows.add(new Show(6, 3, "Music and Lyrics", 75, 40));
        this.shows.add(new Show(7, 3, "The Back-up Plan", 65, 60));
        this.shows.add(new Show(8, 4, "WALL-E", 45, 55));
        this.shows.add(new Show(9, 4, "Water For Elephants", 50, 45));
        this.shows.add(new Show(10, 5, "What Happens in Vegas", 65, 65));
        this.shows.add(new Show(11, 6, "Tangled", 55, 40));
        this.shows.add(new Show(12, 6, "The Curious Case of Benjamin Button", 65, 50));
        this.shows.add(new Show(13, 7, "Rachel Getting Married", 40, 60));
        this.shows.add(new Show(14, 7, "New Year's Eve", 35, 45));
        this.shows.add(new Show(15, 7, "The Proposal", 45, 55));
        this.shows.add(new Show(16, 8, "The Time Traveler's Wife", 75, 65));
        this.shows.add(new Show(17, 8, "The Invention of Lying", 50, 40));
        this.shows.add(new Show(18, 9, "The Heartbreak Kid", 60, 50));
        this.shows.add(new Show(19, 10, "The Duchess", 70, 60));
        this.shows.add(new Show(20, 10, "Mamma Mia!", 40, 45));
    }

    public List<Show> shows(){
        return this.shows;
    }
}

