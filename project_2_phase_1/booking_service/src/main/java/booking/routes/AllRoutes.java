package booking.routes;

import static akka.http.javadsl.server.Directives.*;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.http.javadsl.server.Route;
import booking.actors.BookingActor;

public class AllRoutes {

	   private final static Logger log = LoggerFactory.getLogger(AllRoutes.class);
	   private final TheatreRoutes theatreRoutes;
	   private final ShowRoutes showRoutes;
	   private final BookingRoutes bookingRoutes;
	   
	   public AllRoutes(TheatreRoutes theatreRoutes , ShowRoutes showRoutes, BookingRoutes bookingRoutes) {
		   this.theatreRoutes = theatreRoutes;
		   this.showRoutes = showRoutes;
		   this.bookingRoutes = bookingRoutes;
	   }
	   
	   //main route class, it will forward the request to appropriate route
	   public Route allRoutes() {
		   return 
			  concat(
				  pathPrefix("theatres", () -> this.theatreRoutes.theatreRoutes()),
				  concat(pathPrefix("bookings", () -> this.bookingRoutes.bookingRoutes()),
				         pathPrefix("shows", ()-> this.showRoutes.showRoutes())
				         )
			         );
	   }
	   
}
