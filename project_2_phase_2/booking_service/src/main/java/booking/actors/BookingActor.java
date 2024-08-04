package booking.actors;

import java.util.*;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import booking.CborSerializable;
import booking.actors.WorkerActor.BookingsList;
import booking.actors.WorkerActor.GetShowsResponse;
import booking.entites.*;

public class BookingActor extends AbstractBehavior<BookingActor.Command> {

   // actor protocol
   public sealed interface Command extends CborSerializable{}
   
   public final static record GetTheatres(ActorRef<Theatres> replyTo) implements Command {}
   
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public final static record Theatre(int id, String name, String location) implements CborSerializable {}
   
   public final static record Theatres(List<Theatre> theatres) implements CborSerializable {}
   
   private final List<Theatre> theatres = new ArrayList<>();
   
   //command to get the list of shows with given theatre_id
   public final static record GetShows(int theatre_id, ActorRef<GetShowsResponse> replyTo) implements Command {}
   
   //command to get the response from showActors the list of shows
   public final static record ReturnedShowList(ShowList showList) implements Command {}
   
   //command to get show with show_id
   public final static record GetShowById(int show_id, ActorRef<GetShowResponse> replyTo) implements Command {}
   
   //command to create a booking
   public final static record CreateBooking(Booking booking, ActorRef<Booking> replyTo) implements Command {}
   
   //command to get the list of bookings with user_id
   public final static record GetBookings(int userId, ActorRef<BookingsList> replyTo) implements Command {}

   //command to delete the bookings
   public final static record DeleteBookings(int user_id,int show_id, ActorRef<Boolean> replyTo) implements Command {}
   
   //actorRef of workerActors
   ActorRef<WorkerActor.Command> worker;

   //counter used to generate booking_ids
   private int bookingId;

   //function to populate the theatre list
   private void populateTheatres() {
	    theatres.add(new Theatre(1,"Helen Hayes Theater","240 W 44th St."));
	    theatres.add(new Theatre(2,"Cherry Lane Theatre","38 Commerce Street"));
	    theatres.add(new Theatre(3,"New World Stages","340 West 50th Street"));
	    theatres.add(new Theatre(4,"The Zipper Theater","100 E 17th St"));
	    theatres.add(new Theatre(5,"Queens Theatre","Meadows Corona Park"));
	    theatres.add(new Theatre(6,"The Public Theater","425 Lafayette St"));
	    theatres.add(new Theatre(7,"Manhattan Ensemble Theatre","55 Mercer St."));
	    theatres.add(new Theatre(8,"Metropolitan Playhouse","220 E 4th St."));
	    theatres.add(new Theatre(9,"Acorn Theater","410 West 42nd Street"));
	    theatres.add(new Theatre(10,"Apollo Theater","253 West 125th Street"));
   }
  
   //constructor of bookingActor
   private BookingActor(ActorContext<Command> context, ActorRef<WorkerActor.Command> worker) {
	    super(context);
	    populateTheatres();
	    this.worker=worker;
		this.bookingId=0;
	  }

   public static Behavior<Command> create(ActorRef<WorkerActor.Command> worker) {
	    return Behaviors.setup(context -> new BookingActor(context, worker));
	  }
   
   @Override
   public Receive<Command> createReceive() {
     return newReceiveBuilder()
    		 .onMessage(GetTheatres.class, this::onGetTheatres)
    		 .onMessage(GetShows.class, this::onGetShows)
    		 .onMessage(GetShowById.class, this::onGetShowById)
    		 .onMessage(CreateBooking.class, this::onCreateBooking)
    		 .onMessage(GetBookings.class, this::onGetBookings)
    		 .onMessage(DeleteBookings.class, this::onDeleteBookings)
    		 .build();
   }
   
   //returns the list of all theatres
   private Behavior<Command> onGetTheatres(GetTheatres command){
	   //reply the list of theatres
	   command.replyTo().tell(new Theatres(Collections.unmodifiableList(new ArrayList<>(theatres))));
	   return this;
   }
   
   //Check the theatre_id and tell each show Actor to fill there show information into the list
   private Behavior<Command> onGetShows(GetShows command){
	   //check if the theatre with theatre_id exists
	   boolean valid = theatres.stream().filter(theatre -> Integer.valueOf(theatre.id()).equals(command.theatre_id())).findFirst().isPresent();
	   if(valid) {
		   //send command to worker to get shows
	   	   worker.tell(new WorkerActor.GetShows(command.theatre_id(), command.replyTo()));
	   }
	   //case when the theatre_id is invalid
	   else {
	   command.replyTo().tell(new GetShowsResponse(valid,null));
	   }
	   return this;
   }
   
   //returns the show with given show_id
   private Behavior<Command> onGetShowById(GetShowById command){
	   //forward the request to worker
	   worker.tell(new WorkerActor.GetShowById(command.show_id(), command.replyTo()));
	   return this;
   }
   
   //create a new booking
   private Behavior<Command> onCreateBooking(CreateBooking command){
	   //send the request to worker
	   command.booking().setId(bookingId);
	   bookingId++;
	   worker.tell(new WorkerActor.CreateBooking(command.booking(), command.replyTo()));
	   return this;
   }
   
   //returns the list of bookings with given user_id
   private Behavior<Command> onGetBookings(GetBookings command){
	   //send the request to worker
	   worker.tell(new WorkerActor.GetBookings(command.userId(), command.replyTo()));
	   return this;
   }
   
   //handle the request to delete bookings
   private Behavior<Command> onDeleteBookings(DeleteBookings command){
	   //send the request to worker
	   worker.tell(new WorkerActor.DeleteBookings(command.user_id(), command.show_id(), command.replyTo()));
	   return this;
   }
}
