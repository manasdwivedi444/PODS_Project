package booking.actors;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import booking.CborSerializable;
import booking.actors.BookingActor.Theatres;
import booking.entites.Booking;
import booking.entites.GetBookingsResponse;
import booking.entites.GetShowResponse;
import booking.entites.HasBooking;
import booking.entites.InitShowList;
import booking.entites.Show;
import booking.entites.ShowList;

public class ShowActor extends AbstractBehavior<ShowActor.Command>{
	
	public sealed interface Command extends CborSerializable{}
	
	//to remove later
	public final static record GetTheatres(ActorRef<Theatres> replyTo) implements Command {}
	
	public final static record show(int id, int theatre_id, String title, int price, int seats_available) implements CborSerializable {}
	
	public final static record GetShow(ActorRef<GetShowResponse> replyTo) implements Command {}
	
	public final static record GetShowByTheatreId(ShowList showList,ActorRef<WorkerActor.Command> replyTo) implements Command {}
	
	public final static record CreateBooking(Booking booking, ActorRef<Booking> replyTo) implements Command {}
	
	public final static record GetBookings(GetBookingsResponse getBookingsResponse, ActorRef<WorkerActor.Command> replyTo) implements Command {}
	
	public final static record DeleteBookings(HasBooking hasBooking, ActorRef<WorkerActor.Command> replyTo) implements Command {}
	
	//stores the information of this show
	private Show showInfo;
	private static InitShowList initShowList = new InitShowList();
	
	//stores the list of bookings for this show
	private final List<Booking> bookings = new ArrayList<>();
	
	private static final String USER_SERVICE_URL = "http://localhost:8080/users/";
    private static final String WALLET_SERVICE_URL = "http://localhost:8082/wallets";
	
	//entity typekey for showActor
    public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(ShowActor.Command.class, "ShowEntity");

    //constructor of ShowActor
	private ShowActor (ActorContext<Command> context,String id) {
	    super(context);
	    this.showInfo = new Show(initShowList.shows().get(Integer.parseInt(id)-1));
	};
	
	public static Behavior<Command> create(String id) {
	    return Behaviors.setup(context->new ShowActor(context,id));
	}
	
	@Override
	public Receive<Command> createReceive() {
	    return newReceiveBuilder()
	    		 .onMessage(GetShow.class, this::onGetShow)
	    		 .onMessage(GetShowByTheatreId.class, this::onGetShowByTheatreId)
	    		 .onMessage(CreateBooking.class, this::onCreateBooking)
	    		 .onMessage(GetBookings.class, this::onGetBookings)
	    		 .onMessage(DeleteBookings.class, this::onDeleteBookings)
	    		 .build();
	}
	
	//returns the details of this show
	private Behavior<Command> onGetShow(GetShow command){
		Show show=new Show(this.showInfo);
		command.replyTo().tell(new GetShowResponse(true,show));
		return this;
	}
	
	//returns the details of this show if the theatre_id matches
	private Behavior<Command> onGetShowByTheatreId(GetShowByTheatreId command){
		//check if the theatre_id matches
		if(this.showInfo.getTheatreId()==command.showList().getTheatreId()) {
			//add this showinfo to the response
			GetShowResponse showResponse = new GetShowResponse(true,new Show(this.showInfo));
			command.showList.setShowResponse(showResponse);
		}
		//if theatre_id doesn't matches set null response
		else {
			GetShowResponse showResponse = new GetShowResponse(false,new Show(this.showInfo));
			command.showList.setShowResponse(showResponse);
		}
		//send the response to workerActor
		command.replyTo().tell(new WorkerActor.ReturnedShowList(command.showList()));
		return this;
	}
	
	//create a new booking
	private Behavior<Command> onCreateBooking(CreateBooking command){
		//when sufficient seats are not available
		if(command.booking().getSeatsBooked()>this.showInfo.getSeatsAvailable()) {
			command.booking().setId(-1);
			command.replyTo().tell(command.booking());
		}
		else {
			int totalPrice = command.booking().getSeatsBooked()*this.showInfo.getPrice();
			//check if user is valid
			try {
				URL url = new URL(USER_SERVICE_URL+command.booking().getUserId());
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				int code = con.getResponseCode();
				if(code==404) {
					command.booking().setId(-1);
					command.replyTo().tell(command.booking());
					return this;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			//check if user has sufficient balance and deduct required amount
			try {
				URL url = new URL(WALLET_SERVICE_URL+"/"+command.booking().getUserId());
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("PUT");
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("Accept", "application/json");
				con.setDoOutput(true);
				String jsonInputString = "{\"action\": \"debit\", \"amount\": "+totalPrice+"}";
				byte[] input = jsonInputString.getBytes("utf-8");
				OutputStream os = con.getOutputStream();
			    os.write(input, 0, input.length);
			    os.flush();
				os.close();
				int code = con.getResponseCode();
				if(code==400) {
					command.booking().setId(-1);
					command.replyTo().tell(command.booking());
					return this;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			//add the booking to the list of booking, deduct the seats and send the response
			this.bookings.add(command.booking());
			this.showInfo.setSeatsAvailable(this.showInfo.getSeatsAvailable()-command.booking().getSeatsBooked());
			command.replyTo().tell(command.booking());
		}
		return this;
	}
	
	//handle the requsets to send the list of bookings with given user_id
	private Behavior<Command> onGetBookings(GetBookings command){
		//iterate through the list of bookings and add the booking with matching user_id
		for(Booking booking : this.bookings) {
			if(booking.getUserId()==command.getBookingsResponse().getUserId()) {
				command.getBookingsResponse().getTotalBookings().add(booking);
			}
		}
		//send the reply to workerActor
		command.replyTo().tell(new WorkerActor.ReturnedBookingList(command.getBookingsResponse()));
		return this;
	}
	
	//handle the request to delete bookings
	private Behavior<Command> onDeleteBookings(DeleteBookings command){
		List<Booking> bookingsToDelete=new ArrayList<>();
		boolean valid=false;
		//iterate through the list of bookings and remove appropriate booking
		for(Booking booking:this.bookings) {
			//userId is set to -1 when no userId is mentioned
			if(booking.getUserId()==command.hasBooking().getUserId()||command.hasBooking().getUserId()==-1) {
				valid=true;
				bookingsToDelete.add(booking);
				this.showInfo.setSeatsAvailable(this.showInfo.getSeatsAvailable()+booking.getSeatsBooked());
				//update the balance of the user
				int totalPrice = booking.getSeatsBooked()*this.showInfo.getPrice();
				try {
					URL url = new URL(WALLET_SERVICE_URL+"/"+booking.getUserId());
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("PUT");
					con.setRequestProperty("Content-Type", "application/json");
					con.setRequestProperty("Accept", "application/json");
					con.setDoOutput(true);
					String jsonInputString = "{\"action\": \"credit\", \"amount\": "+totalPrice+"}";
					byte[] input = jsonInputString.getBytes("utf-8");
					OutputStream os = con.getOutputStream();
				    os.write(input, 0, input.length);
				    os.flush();
					os.close();
					int code = con.getResponseCode();
					if(code==400) {
						System.out.println("error update wallet");
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.bookings.removeAll(bookingsToDelete);
		//if delete is for a specific show then directly send the response
		if(command.hasBooking().getShowId()!=-1) {
			if(valid) {
				command.hasBooking().getReplyTo().tell(true);
			}
			else {
				command.hasBooking().getReplyTo().tell(false);
			}
		}
		//else send response to workerActor
		else {
			command.replyTo().tell(new WorkerActor.ReturnedDeleteBookings(command.hasBooking(), valid));
		}
		return this;
	}

}
