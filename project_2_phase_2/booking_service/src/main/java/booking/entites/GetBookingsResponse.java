package booking.entites;

import java.util.List;

import akka.actor.typed.ActorRef;
import booking.CborSerializable;
import booking.actors.WorkerActor.BookingsList;

public class GetBookingsResponse implements CborSerializable {
   private List<Booking> totalBookings;
   private Counter counter;
   private int userId;
   private ActorRef<BookingsList> replyTo;
   
   public GetBookingsResponse(List<Booking> totalBookings, Counter counter, int userId, ActorRef<BookingsList> replyTo) {
	   this.totalBookings=totalBookings;
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
   
   public Counter getCounter() {
	   return this.counter;
   }
   
   public ActorRef<BookingsList> getReplyTo(){
	   return this.replyTo;
   }
   
}
