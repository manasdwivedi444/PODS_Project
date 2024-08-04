package booking.entites;

import java.util.List;

import akka.actor.typed.ActorRef;
import booking.actors.BookingActor.GetShowsResponse;

public class ShowList {
    List<Show> shows;
    Counter counter;
    GetShowResponse showResponse;
    //has valid and a show object
    ActorRef<GetShowsResponse> replyTo;
    int theatre_id;
    
    public ShowList(Counter counter,List<Show> shows, ActorRef<GetShowsResponse> replyTo, int theatre_id) {
    	this.shows=shows;
    	this.counter=counter;
    	this.replyTo=replyTo;
    	this.theatre_id=theatre_id;
    }
    
    public Counter getCounter() {
    	return this.counter;
    }
    
    public ActorRef<GetShowsResponse> getReplyTo(){
    	return this.replyTo;
    }
    
    public void setShowResponse(GetShowResponse showResponse) {
    	this.showResponse=showResponse;
    }
    
    public GetShowResponse getShowResponse() {
    	return this.showResponse;
    }
    
    public void addShow(Show show) {
    	this.shows.add(show);
    }
    
    public int getTheatreId() {
    	return this.theatre_id;
    }
    
    public List<Show> getShows(){
    	return this.shows;
    }
    
}
