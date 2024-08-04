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
import booking.entites.*;

public class BookingActor extends AbstractBehavior<BookingActor.Command> {

   // actor protocol
   public sealed interface Command {}
   
   public final static record GetTheatres(ActorRef<Theatres> replyTo) implements Command {}
   
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public final static record Theatre(int id, String name, String location) {}
   
   public final static record Theatres(List<Theatre> theatres) {}
   
   private final List<Theatre> theatres = new ArrayList<>();

   public final static record GetShowsResponse(boolean valid, List<Show> shows) {}
   
   //command to get the list of shows with given theatre_id
   public final static record GetShows(int theatre_id, ActorRef<GetShowsResponse> replyTo) implements Command {}
   
   //command to get the response from showActors the list of shows
   public final static record ReturnedShowList(ShowList showList) implements Command {}
   
   //command to get show with show_id
   public final static record GetShowById(int show_id, ActorRef<GetShowResponse> replyTo) implements Command {}
   
   //command to create a booking
   public final static record CreateBooking(Booking booking, ActorRef<Booking> replyTo) implements Command {}
   
   //command to get the list of bookings with user_id
   public final static record GetBookings(int userId, ActorRef<List<Booking>> replyTo) implements Command {}
   
   //command to get the response from showActors the list of bookings
   public final static record ReturnedBookingList(GetBookingsResponse getBookingsResponse) implements Command {}

   //command to delete the bookings
   public final static record DeleteBookings(int user_id,int show_id, ActorRef<Boolean> replyTo) implements Command {}
   
   //command to get response from showActors on delete
   public final static record ReturnedDeleteBookings(HasBooking hasBooking, boolean valid) implements Command {}
   
   //stores the map of all the showActors
   public final HashMap<Integer,ActorRef<ShowActor.Command>> showActors= new HashMap<>();
   
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
   
   //function to spawn all showActors
   private void populateShows(ActorContext<Command> context) {
	    showActors.put(1, context.spawn(ShowActor.create(1, 1, "Youth in Revolt", 50, 40),"show_1"));
	    showActors.put(2, context.spawn(ShowActor.create(2, 1, "Leap Year", 55, 30),"show_2"));
	    showActors.put(3, context.spawn(ShowActor.create(3, 1, "Remember Me", 60, 55),"show_3"));
	    showActors.put(4, context.spawn(ShowActor.create(4, 2, "Fireproof", 65, 65),"show_4"));
	    showActors.put(5, context.spawn(ShowActor.create(5, 2, "Beginners", 55, 50),"show_5"));
	    showActors.put(6, context.spawn(ShowActor.create(6, 3, "Music and Lyrics", 75, 40),"show_6"));
	    showActors.put(7, context.spawn(ShowActor.create(7, 3, "The Back-up Plan", 65, 60),"show_7"));
	    showActors.put(8, context.spawn(ShowActor.create(8, 4, "WALL-E", 45, 55),"show_8"));
	    showActors.put(9, context.spawn(ShowActor.create(9, 4, "Water For Elephants", 50, 45),"show_9"));
	    showActors.put(10, context.spawn(ShowActor.create(10, 5, "What Happens in Vegas", 65, 65),"show_10"));
	    showActors.put(11, context.spawn(ShowActor.create(11, 6, "Tangled", 55, 40),"show_11"));
	    showActors.put(12, context.spawn(ShowActor.create(12, 6, "The Curious Case of Benjamin Button", 65, 50),"show_12"));
	    showActors.put(13, context.spawn(ShowActor.create(13, 7, "Rachel Getting Married", 40, 60),"show_13"));
	    showActors.put(14, context.spawn(ShowActor.create(14, 7, "New Year's Eve", 35, 45),"show_14"));
	    showActors.put(15, context.spawn(ShowActor.create(15, 7, "The Proposal", 45, 55),"show_15"));
	    showActors.put(16, context.spawn(ShowActor.create(16, 8, "The Time Traveler's Wife", 75, 65),"show_16"));
	    showActors.put(17, context.spawn(ShowActor.create(17, 8, "The Invention of Lying", 50, 40),"show_17"));
	    showActors.put(18, context.spawn(ShowActor.create(18, 9, "The Heartbreak Kid", 60, 50),"show_18"));
	    showActors.put(19, context.spawn(ShowActor.create(19, 10, "The Duchess", 70, 60),"show_19"));
	    showActors.put(20, context.spawn(ShowActor.create(20, 10, "Mamma Mia!", 40, 45),"show_20"));
   }
  
   //constructor of bookingActor
   private BookingActor(ActorContext<Command> context) {
	    super(context);
	    populateTheatres();
	    populateShows(context);
	    this.bookingId=0;
	  }

   public static Behavior<Command> create() {
	    return Behaviors.setup(context -> new BookingActor(context));
	  }
   
   @Override
   public Receive<Command> createReceive() {
     return newReceiveBuilder()
    		 .onMessage(GetTheatres.class, this::onGetTheatres)
    		 .onMessage(GetShows.class, this::onGetShows)
    		 .onMessage(ReturnedShowList.class, this::onReturnedShowList)
    		 .onMessage(GetShowById.class, this::onGetShowById)
    		 .onMessage(CreateBooking.class, this::onCreateBooking)
    		 .onMessage(GetBookings.class, this::onGetBookings)
    		 .onMessage(ReturnedBookingList.class, this::onReturnedBookingList)
    		 .onMessage(DeleteBookings.class, this::onDeleteBookings)
    		 .onMessage(ReturnedDeleteBookings.class, this::onReturnedDeleteBookings)
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
	   	   List<Show> shows = new ArrayList<>();
	   	   Counter counter=new Counter();
	   	   //tell each showActor to fill its details if the theatre_id exists
		   for(ActorRef<ShowActor.Command> showActor : showActors.values()) {
			   showActor.tell(new ShowActor.GetShowByTheatreId(new ShowList(counter,shows,command.replyTo(),command.theatre_id()), getContext().getSelf()));
		   }
	   }
	   //case when the theatre_id is invalid
	   else {
	   command.replyTo().tell(new GetShowsResponse(valid,null));
	   }
	   return this;
   }
   
   //Handle the reply from showActors 
   private Behavior<Command> onReturnedShowList(ReturnedShowList command){
	   //increment the counter to keep track of how many showActors has replied
	   command.showList().getCounter().increment();
	   //add the show information if the theatre_id is valid
	   if(command.showList().getShowResponse().getValid()==true) {
		   command.showList().addShow(command.showList().getShowResponse().getShow());
	   }
	   //when all the showActors has replied send the list of shows as response
	   if(command.showList().getCounter().getCount()==this.showActors.size()) {
		   command.showList().getReplyTo().tell(new GetShowsResponse(true,command.showList().getShows()));
	   }
	   return this;
   }
   
   //returns the show with given show_id
   private Behavior<Command> onGetShowById(GetShowById command){
	   //forward the request to appropriate showActor
	   if(showActors.containsKey(command.show_id())) {
		   this.showActors.get(command.show_id()).tell(new ShowActor.GetShow(command.replyTo()));
	   }
	   //when no show with the given id exists
	   else {
	       command.replyTo().tell(new GetShowResponse(false,null));
	   }
	   return this;
   }
   
   //create a new booking
   private Behavior<Command> onCreateBooking(CreateBooking command){
	   //send the request to appropriate showActor
	   if(this.showActors.containsKey(command.booking().getShowId())) {
		   this.bookingId++;
		   command.booking().setId(this.bookingId);
		   this.showActors.get(command.booking().getShowId()).tell(new ShowActor.CreateBooking(command.booking(),command.replyTo()));
	   }
	   //when the given show_id is invalid
	   else {
		   command.booking().setId(-1);
		   command.replyTo().tell(command.booking());
	   }
	   return this;
   }
   
   //returns the list of bookings with given user_id
   private Behavior<Command> onGetBookings(GetBookings command){
	   List<Booking> totalBookings = new ArrayList<>();
	   Counter counter=new Counter();
	   //this class will hold the responses from each showActor
	   GetBookingsResponse getBookingsResponse = new GetBookingsResponse(totalBookings,counter,command.userId(),command.replyTo());
	   //forward the request to each showActor so that they can respond with their respective list of bookings
	   for(ActorRef<ShowActor.Command> showActor : showActors.values()) {
		   showActor.tell(new ShowActor.GetBookings(getBookingsResponse, getContext().getSelf()));
	   }
	   return this;
   }
   
   //handle the returned list of bookings from showActors
   private Behavior<Command> onReturnedBookingList(ReturnedBookingList command){
	   //increment the counter to keep track of how many showActors has responded
	   command.getBookingsResponse().getCounter().increment();
	   //add the responded list with the list of bookings
	   command.getBookingsResponse().getTotalBookings().addAll(command.getBookingsResponse().getShowBookings());
	   //when all the showActors has responded with there list of bookings send the response
	   if(command.getBookingsResponse().getCounter().getCount()==this.showActors.size()) {
		   command.getBookingsResponse().getReplyTo().tell(command.getBookingsResponse().getTotalBookings());
	   }
	   return this;
   }
   
   //handle the request to delete bookings
   private Behavior<Command> onDeleteBookings(DeleteBookings command){
	   Counter counter = new Counter();
	   HasBooking hasBooking = new HasBooking(command.show_id(),command.user_id(),counter,command.replyTo());
	   //when show_id is not mentioned send the delete request to first showActor in the hashmap
	   if(command.show_id()==-1) {
		   this.showActors.get(1).tell(new ShowActor.DeleteBookings(hasBooking,  getContext().getSelf()));
	   }
	   //when the show_id is mentioned send the request to that
	   else {
		   if(this.showActors.containsKey(command.show_id())) {
			   this.showActors.get(command.show_id()).tell(new ShowActor.DeleteBookings(hasBooking,  getContext().getSelf()));
		   }
		   //when the show_id is invalid
		   else {
			   command.replyTo().tell(false);
		   }
	   }
	   return this;
   }
   
   //handle the response from showActors on delete
   private Behavior<Command> onReturnedDeleteBookings(ReturnedDeleteBookings command){
	   //valid is returned true if there is atleast on booking with the given parameters
	   if(command.valid()==true) {
		   command.hasBooking().setValid(true);
	   }
	   //increment the counter and check if all the showActors has responded
	   command.hasBooking().getCounter().increment();
	   //if all the showActors has responded return the response
	   if(command.hasBooking().getCounter().getCount()==this.showActors.size()) {
		   if(command.hasBooking().getVaild()) {
			   command.hasBooking().getReplyTo().tell(true);
		   }
		   else {
			   command.hasBooking().getReplyTo().tell(false);
		   }
	   }
	   //else send the delete response to next showActor
	   else {
		   this.showActors.get(command.hasBooking().getCounter().getCount()+1).tell(new ShowActor.DeleteBookings(command.hasBooking(),  getContext().getSelf()));
	   }
	   return this;
   }
   
}
