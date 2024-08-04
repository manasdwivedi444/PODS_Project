package booking.entites;

import java.util.List;

import akka.actor.typed.ActorRef;

public class GetBookingsResponse {
   private List<Booking> showBookings;
   private List<Booking> totalBookings;
   private Counter counter;
   private int userId;
   private ActorRef<List<Booking>> replyTo;
   
   public GetBookingsResponse(List<Booking> totalBookings, Counter counter, int userId, ActorRef<List<Booking>> replyTo) {
	   this.totalBookings=totalBookings;
	   this.counter=counter;
	   this.replyTo=replyTo;
	   this.userId=userId;
   }
   
   public GetBookingsResponse(List<Booking> totalBookings,List<Booking> showBookings, Counter counter, int userId, ActorRef<List<Booking>> replyTo) {
	   this.totalBookings=totalBookings;
	   this.showBookings=showBookings;
	   this.counter=counter;
	   this.replyTo=replyTo;
	   this.userId=userId;
   }
   
   public int getUserId() {
	   return this.userId;
   }
   
   public List<Booking> getTotalBookings(){
	   return this.totalBookings;
   }
   
   public List<Booking> getShowBookings(){
	   return this.showBookings;
   }
   
   public Counter getCounter() {
	   return this.counter;
   }
   
   public ActorRef<List<Booking>> getReplyTo(){
	   return this.replyTo;
   }
   
}
