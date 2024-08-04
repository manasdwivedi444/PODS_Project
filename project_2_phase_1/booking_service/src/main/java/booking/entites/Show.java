package booking.entites;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Show {
    private int id;
    private int theatre_id;
    private String title;
    private int price;
    private int seats_available;
    
    public Show(int id, int theatre_id, String title, int price, int seats_available) {
    	this.id=id;
    	this.theatre_id=theatre_id;
    	this.title=title;
    	this.price=price;
    	this.seats_available=seats_available;
    }
    
    public Show(Show show) {
    	this.id=show.getId();
    	this.theatre_id=show.getTheatreId();
    	this.title=show.getTitle();
    	this.price=show.getPrice();
    	this.seats_available=show.getSeatsAvailable();
	}

	public int getId() {
    	return this.id;
    }
    
    public int getTheatreId() {
    	return this.theatre_id;
    }
    
    public String getTitle() {
    	return this.title;
    }
    
    public int getPrice() {
    	return this.price;
    }
    
    public int getSeatsAvailable() {
    	return this.seats_available;
    }
    
    public int setSeatsAvailable(int seats_available) {
    	this.seats_available=seats_available;
    	return this.seats_available;
    }
    
}
