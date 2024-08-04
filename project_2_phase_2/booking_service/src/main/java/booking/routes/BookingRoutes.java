package booking.routes;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.*;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import booking.actors.BookingActor;
import booking.actors.WorkerActor.BookingsList;
import booking.entites.Booking;

public class BookingRoutes {
	   private final ActorRef<BookingActor.Command> bookingActor;
	   private final Duration askTimeout;
	   private final Scheduler scheduler;
	   
	   public BookingRoutes(ActorSystem<?> system, ActorRef<BookingActor.Command> bookingActor) {
			  this.bookingActor = bookingActor;
			  scheduler = system.scheduler();
			  askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
		   }
	   
	   private CompletionStage<Booking> createBooking(Booking booking){
		   return AskPattern.ask(bookingActor, ref -> new BookingActor.CreateBooking(booking,ref) , askTimeout, scheduler);
	   }

	   private CompletionStage<BookingsList> getBookings(int user_id){
		   return AskPattern.ask(bookingActor, ref-> new BookingActor.GetBookings(user_id, ref), askTimeout, scheduler);
	   }
	   
	   private CompletionStage<Boolean> deleteBookings(int user_id,int show_id){
		   return AskPattern.ask(bookingActor, ref -> new BookingActor.DeleteBookings(user_id, show_id, ref), askTimeout, scheduler);
	   }
	   
	   //route for all urls beginning with /bookings
	   public Route bookingRoutes() {
			  return concat( pathEnd( () -> 
			                     concat( post(() ->  entity( Jackson.unmarshaller(Booking.class), booking ->
			                                             onSuccess(createBooking(booking) , newbooking -> { if(newbooking.getId()==-1) return complete(StatusCodes.BAD_REQUEST, newbooking, Jackson.marshaller());
			                                                                                               else return complete(StatusCodes.OK, newbooking, Jackson.marshaller());}))),
			                    		 delete(() -> onSuccess(deleteBookings(-1,-1), valid -> complete(StatusCodes.OK) )))
					         ),
					         pathPrefix("users",() -> 
					              pathPrefix( integerSegment(), user_id ->
					                  concat( pathEnd (() ->
					                              concat( get (() -> onSuccess(getBookings(user_id), bookings -> complete(StatusCodes.OK, bookings.bookings(), Jackson.marshaller()))),
					                        		 delete (() -> onSuccess(deleteBookings(user_id,-1), valid -> { if(valid) return complete(StatusCodes.OK);
					                        		                                                                else return complete(StatusCodes.NOT_FOUND);})))
					                          ),
					                		 path( segment("shows").slash(integerSegment()), show_id ->
					                		          delete (() -> onSuccess(deleteBookings(user_id,show_id), valid -> { if(valid) return complete(StatusCodes.OK);
                                                                                                                         else return complete(StatusCodes.NOT_FOUND);}))
					                		  )
					                    )
					               )
					         )
					  );
		   }
}
