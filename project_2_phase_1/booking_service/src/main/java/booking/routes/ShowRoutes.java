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
import booking.actors.BookingActor.GetShowsResponse;
import booking.actors.ShowActor;
import booking.entites.Show;

public class ShowRoutes {

	   private final ActorRef<BookingActor.Command> bookingActor;
	   private final Duration askTimeout;
	   private final Scheduler scheduler;
	   
	   public ShowRoutes(ActorSystem<?> system, ActorRef<BookingActor.Command> bookingActor) {
			  this.bookingActor = bookingActor;
			  scheduler = system.scheduler();
			  askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
		   }
	   
	   private CompletionStage<GetShowsResponse> getShows(int theatre_id) {
		   return AskPattern.ask(bookingActor, ref -> new BookingActor.GetShows(theatre_id,ref) , askTimeout, scheduler);
	   }
	   
	   private CompletionStage<booking.entites.GetShowResponse> getShowById(int show_id) {
		   return AskPattern.ask(bookingActor, ref -> new BookingActor.GetShowById(show_id,ref) , askTimeout, scheduler);
	   }
	   
	   //route for all urls beginning with /shows
	   public Route showRoutes() {
			  return concat( path(integerSegment(), show_id ->
                                  get(() -> onSuccess(getShowById(show_id), show -> {
                                	                                               if(show.getValid()==true) return complete(StatusCodes.OK, show.getShow(), Jackson.marshaller());
                                	                                               else return complete(StatusCodes.NOT_FOUND);}))),
					         path(segment("theatres").slash(integerSegment()), theatre_id ->
			                      get(() -> onSuccess(getShows(theatre_id), shows -> {
			                    	    if(shows.valid()) return complete(StatusCodes.OK, shows.shows(), Jackson.marshaller());
			                    	    else return complete(StatusCodes.NOT_FOUND);
			                      })))
					  );
		   }
}
