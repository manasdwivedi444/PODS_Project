package booking.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import booking.CborSerializable;
import booking.entites.Booking;
import booking.entites.Counter;
import booking.entites.GetBookingsResponse;
import booking.entites.GetShowResponse;
import booking.entites.HasBooking;
import booking.entites.Show;
import booking.entites.ShowList;

public class WorkerActor extends AbstractBehavior<WorkerActor.Command>{
   // actor protocol
   public sealed interface Command extends CborSerializable{}
   
   //public final static record GetTheatres(ActorRef<Theatres> replyTo) implements Command {}
   
   //public final static record Theatres(List<Theatre> theatres) {}
   
   //private final List<Theatre> theatres = new ArrayList<>();

   public final static record GetShowsResponse(boolean valid, List<Show> shows) implements CborSerializable {}
   
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

   public final static record BookingsList(List<Booking> bookings) implements Command {}
   
   //command to get the response from showActors the list of bookings
   public final static record ReturnedBookingList(GetBookingsResponse getBookingsResponse) implements Command {}

   //command to delete the bookings
   public final static record DeleteBookings(int user_id,int show_id, ActorRef<Boolean> replyTo) implements Command {}
   
   //command to get response from showActors on delete
   public final static record ReturnedDeleteBookings(HasBooking hasBooking, boolean valid) implements Command {}
   
   public static final ServiceKey<WorkerActor.Command> serviceKey = ServiceKey.create(WorkerActor.Command.class, "worker");

   //stores the map of all the showActors
   public final HashMap<Integer,EntityRef<ShowActor.Command>> showActors= new HashMap<>();
 
   //constructor of workerActor
   private WorkerActor(ActorContext<Command> context, List<EntityRef<ShowActor.Command>> showActors) {
	    super(context);
        int index=1;
        for(EntityRef<ShowActor.Command>showActor : showActors){
            this.showActors.put(index, showActor);
            index++;
        }
	}

   public static Behavior<Command> create(List<EntityRef<ShowActor.Command>> showActors) {
	    return Behaviors.setup(context -> new WorkerActor(context,showActors));
	  }
   
   @Override
   public Receive<Command> createReceive() {
     return newReceiveBuilder()
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

   //tell each show Actor to fill there show information into the list
   private Behavior<Command> onGetShows(GetShows command){
	   	List<Show> shows = new ArrayList<>();
	   	Counter counter=new Counter();
	   	//tell each showActor to fill its details if the theatre_id exists
		showActors.get(1).tell(new ShowActor.GetShowByTheatreId(new ShowList(counter,shows,command.replyTo(),command.theatre_id()), getContext().getSelf()));
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
       else{
        showActors.get(command.showList().getCounter().getCount()+1).tell(new ShowActor.GetShowByTheatreId(command.showList(), getContext().getSelf()));
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
           //System.out.println(command.booking().getId());
           //System.out.println(command.booking().getUserId());
           //System.out.println(command.booking().getShowId());
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
	   showActors.get(1).tell(new ShowActor.GetBookings(getBookingsResponse, getContext().getSelf()));
	   return this;
   }
   
   //handle the returned list of bookings from showActors
   private Behavior<Command> onReturnedBookingList(ReturnedBookingList command){
	   //increment the counter to keep track of how many showActors has responded
	   command.getBookingsResponse().getCounter().increment();
	   //when all the showActors has responded with there list of bookings send the response
       //System.out.println("counter is at "+command.getBookingsResponse().getCounter().getCount());
       //System.out.println("size is "+this.showActors.size());
	   if(command.getBookingsResponse().getCounter().getCount()==this.showActors.size()) {
		   command.getBookingsResponse().getReplyTo().tell(new BookingsList(command.getBookingsResponse().getTotalBookings()));
	   }
       else{
           showActors.get(command.getBookingsResponse().getCounter().getCount()+1).tell(new ShowActor.GetBookings(command.getBookingsResponse(), getContext().getSelf()));
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
       //System.out.println("counter is at "+command.hasBooking().getCounter().getCount());
       //System.out.println("size is "+this.showActors.size());
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
