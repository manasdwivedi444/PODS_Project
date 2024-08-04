package booking.routes;

import static akka.http.javadsl.server.Directives.*;

import akka.http.javadsl.server.Route;

public class AllRoutes {

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
