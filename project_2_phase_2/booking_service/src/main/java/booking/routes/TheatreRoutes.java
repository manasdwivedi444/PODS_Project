package booking.routes;

import static akka.http.javadsl.server.Directives.*;

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

public class TheatreRoutes {

   private final ActorRef<BookingActor.Command> bookingActor;
   private final Duration askTimeout;
   private final Scheduler scheduler;
   
   public TheatreRoutes(ActorSystem<?> system, ActorRef<BookingActor.Command> bookingActor) {
	  this.bookingActor = bookingActor;
	  scheduler = system.scheduler();
	  askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
   }
   
   private CompletionStage<BookingActor.Theatres> getTheatres() {
	  return AskPattern.ask(bookingActor, BookingActor.GetTheatres::new, askTimeout, scheduler);
   }
   
   //route for all urls beginning with /theatres
   public Route theatreRoutes() {
	  return pathEnd( () ->  
	                         get(() ->
                                        onSuccess(getTheatres(), theatres-> complete(StatusCodes.OK, theatres, Jackson.marshaller())))
                   );
   }
	
}
