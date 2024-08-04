package booking.entites;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import booking.CborSerializable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Booking implements CborSerializable {
    private int id;
    private int show_id;
    private int user_id;
    private int seats_booked;
    
    public Booking() {
    	
    }
    
    public Booking(int id, int show_id, int user_id, int seats_booked) {
    	this.id=id;
    	this.show_id=show_id;
    	this.user_id=user_id;
    	this.seats_booked=seats_booked;
    }
    
    public int getId() {
    	return this.id;
    }
    
    public int getShowId() {
    	return this.show_id;
    }
    
    public int getUserId() {
    	return this.user_id;
    }
    
    public int getSeatsBooked() {
    	return this.seats_booked;
    }
    
    public void setId(int id) {
    	this.id=id;
    }
    
    public void setShowId(int show_id) {
    	this.show_id=show_id;
    }
    
    public void setUserId(int user_id) {
    	this.user_id=user_id;
    }
    
    public void setSeats_Booked(int seats_booked) {
    	this.seats_booked=seats_booked;
    }
    
}
