package com.PODS_Project.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.PODS_Project.entity.Booking;
import com.PODS_Project.entity.Show;
import com.PODS_Project.entity.Theatre;
import com.PODS_Project.service.BookingService;


@Transactional(isolation = Isolation.SERIALIZABLE)
@RestController
public class BookingController {
	@Autowired
	private BookingService bookingService;
	
	@GetMapping("/theatres")
// No input.
// Return List of theatres(as JSON object).
// Always return HttpStatusCode 200(OK).
	public ResponseEntity<?> getAllTheatres(){
		return new ResponseEntity<>(bookingService.getAllTheatres(), HttpStatus.OK);
	}
// Takes theatre_id as PathVariable input.
// Return List of Shows(as JSON objects).
	@GetMapping("/shows/theatres/{theatre_id}")
	public ResponseEntity<?> getShowByTheatreId(@PathVariable("theatre_id") int theatre_id){
		List<Theatre> t = new ArrayList<>();
// t contains all theatre_id's of theatres present in theatreRepository
		t = bookingService.getAllTheatres();
// Checks if input theatre_id is in t	
		int present=0;
		for(int i=0 ; i< t.size();i++ ) {
			if(t.get(i).getId()==theatre_id) {
				present = 1;
			}
		}
		if(present == 0) {
// Executed if there exist no theatre in theatreRepository with Id equal to input theatre_id.
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
// shows contain details about all shows in showRepository
		List<Show> shows = bookingService.getAllShow();
// xshow is to stores all shows whose theatre_id is equal to input theatre_id.
		List<Show> xshow = new ArrayList<>();		
		Show s1 = new Show();
// For every show checks if its theatre_id is equal to input theatre_id
		for(int i=0; i<shows.size(); i++) {
			if(shows.get(i).getTheatre_id()==theatre_id) {
				s1 = shows.get(i);
				xshow.add(s1);
			}
		}
		
		return new ResponseEntity<>(xshow , HttpStatus.OK);
	}
	
	@GetMapping("/shows/{show_id}")
// Takes show_id as PathVariable input.
// Return a JSON object containing details of show with Id equal to input show_id.
	public ResponseEntity<?> getShowById(@PathVariable("show_id") int show_id){
		if(bookingService.getShowById(show_id)==null) {
// Executed if there is no show in showRepository with Id equal to input show_id.
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		else {
// Executed if there exist a show in showRepository with Id equal to input show_id.
			return new ResponseEntity<>(bookingService.getShowById(show_id), HttpStatus.OK);
		}
	}

	@GetMapping("/bookings/users/{user_id}")
// Takes user_id as PathVariable input.
// Return a list of bookings(as JSON objects).
	public ResponseEntity<?> getBookingByUserId(@PathVariable("user_id") int user_id){
		 try {
// Establishes connection with USER microservice and checks if user exist in userRepository with Id equal to input user_id.
				URL url = new URL("http://manas-user-service:8080/users/"+user_id);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				
				int responseCode = connection.getResponseCode();
				
				if (responseCode == HttpURLConnection.HTTP_OK) {
// Executed if user exist in userRepository
// booking contains all bookings
					List<Booking> bookings = bookingService.getAllBooking();
// x will store bookings where user_id (coulmn) equal to user_id(PathVariable).
					List<Booking> x = new ArrayList<>();
// Checking if user_id (coulmn) of a booking is equal to user_id (PathVariable) ort not.
					for(int i=0; i<bookings.size(); i++) {
						if(bookings.get(i).getUser_id()==user_id) {
							x.add(bookings.get(i));
						}
					}
					return new ResponseEntity<>(x , HttpStatus.OK);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
// Returning HttpStatus OK if user is not registered because non existense of user implies non existense of booking for that user.		
		 return new ResponseEntity<>("User Not Registered", HttpStatus.OK);
	}
	
	@PostMapping("/bookings")
// Takes JSON input containing "show_id", "user_id" and "seats_booked" attributes.
// No output (except HttpStatus Codes)
	public ResponseEntity<?> bookMyShow(@RequestBody Booking booking) {
		
		if(bookingService.getShowById(booking.getShow_id())==null) {
// Executed if no show exist with Id equal to show_id attribute of JSON input.
			return new ResponseEntity<>("Show doesn't exist",HttpStatus.BAD_REQUEST);
		}
		try {
// Establishes connection with USER microservice and checking if user exist in userRepository or not
			URL url = new URL("http://manas-user-service:8080/users/"+booking.getUser_id());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			int responseCode = connection.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
// Executed if no User with Id equal to user_id attribute of JSON input exists in userRepository.
				return new ResponseEntity<>("User doesn't exist",HttpStatus.BAD_REQUEST);	
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
// seats_available stores no. of seats available in show with Id equal to JSON input attribute show_id.
		int seats_available =  bookingService.getShowById(booking.getShow_id()).getSeats_available();
// seats_booked stores no. of seats user want to book.
		int seats_booked = booking.getSeats_booked();

		if(seats_booked>seats_available) {
// Will be executed if required no. of seats not available.
			return new ResponseEntity<>("Seats not available", HttpStatus.BAD_REQUEST);
		}
// amount contains amount to be paid in case user is able to book required no. of seats
		int amount = (seats_booked)*(bookingService.getShowById(booking.getShow_id()).getPrice());
		
		try {
// Establish connection with WALLET microservice to check if user have sufficient amount of balance to pay required amount.
			URL url = new URL("http://manas-wallet-service:8080/wallets/"+booking.getUser_id());
			
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("PUT");
	        connection.setRequestProperty("Content-Type", "application/json");
	        connection.setDoOutput(true);
// Sending JSON payload to WALLET microservice to deduct required amount from wallet

	        String payload = "{ \"action\":\"debit\",\"amount\": "+amount+"}";
	        
	        try(OutputStream os = connection.getOutputStream()) {
	            byte[] input = payload.getBytes("utf-8");
	            os.write(input, 0, input.length);
	            os.flush();
	        }
	        
	  

		    int responseCode = connection.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
// Executed if WALLET microservice returns HttpStatusCode 400
				return new ResponseEntity<>("Insufficient Balance",HttpStatus.BAD_REQUEST );
			}else {
// Reduces no. of seats available in that show
				Show show = new Show();
				show = bookingService.getShowById(booking.getShow_id());
				int remaining = seats_available - seats_booked;	
				show.setSeats_available(remaining);
				bookingService.updateSeats(show);
				bookingService.bookMyShow(booking);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return new ResponseEntity<>("Ticket Booked Successfully", HttpStatus.OK);
		
		
	}
	
	@DeleteMapping("/bookings/users/{user_id}/shows/{show_id}")
// Takes user_id and show_id as input in PathVariable
	public ResponseEntity<?> deleteBookingByShow(@PathVariable("user_id") int user_id ,@PathVariable("show_id") int show_id){
		try {
// Establish connection with USER microservice to check if user exist in userRepository.
			URL url = new URL("http://manas-user-service:8080/users/"+user_id);
			
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        connection.setDoOutput(true);
			connection.setRequestMethod("GET");
	        
	        int responseCode = connection.getResponseCode();
			
			if(responseCode!=HttpURLConnection.HTTP_OK) {
				return new ResponseEntity<>("User Doesn't Exist", HttpStatus.NOT_FOUND);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
// booking_id list contain all booking's details
			List<Booking> booking_id = bookingService.getAllBooking();

			int amount;
			Show show = new Show();
// count variable counts no. of booking for in that particular show for that particular user 
			int count=0;
			for(int i =0; i< booking_id.size(); i++) {
				if(booking_id.get(i).getShow_id()==show_id && booking_id.get(i).getUser_id()==user_id) {
					count++;
// amount contains amount that to be added to users wallet after deleting bookings for that show by that user
					amount = booking_id.get(i).getSeats_booked()*bookingService.getShowById(booking_id.get(i).getShow_id()).getPrice();
					try {
// Establishes connection with WALLET microservice to add amount to wallet
						URL url = new URL("http://manas-wallet-service:8080/wallets/"+user_id);
						
						HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				        connection.setDoOutput(true);
						connection.setRequestMethod("PUT");
				        connection.setRequestProperty("Content-Type", "application/json");

				        String payload = "{ \"action\":\"credit\" ,\"amount\": "+amount+"}";
				        
				        try(OutputStream os = connection.getOutputStream()) {
				            byte[] input = payload.getBytes("utf-8");
				            os.write(input, 0, input.length);
				            os.flush();
				        }
				        int responseCode = connection.getResponseCode();
						
						if(responseCode!=HttpURLConnection.HTTP_OK) {
							
						}
							
					} catch (Exception e) {
						e.printStackTrace();
					}
// Updates seats in that show by no. of seats freed by deleting this booking 
					show = bookingService.getShowById(booking_id.get(i).getShow_id());
					show.setSeats_available(show.getSeats_available()+booking_id.get(i).getSeats_booked());

					bookingService.updateSeats(show);
					
					bookingService.deleteBookingByBookingId(booking_id.get(i).getId());
				}
			}
			if(count==0) {
// Executed if no booking for that {show_id,user_id}
				return new ResponseEntity<>("No bookings for this show_id and user_id", HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);
			
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
	}

	@DeleteMapping("/bookings/users/{user_id}")
// Takes a PathVariable user_id as input
// No output 
// Deletes booking for single user 
	public ResponseEntity<?> deleteBookingByUserId(@PathVariable("user_id") int user_id){
		try {
// Establish connection with USER microservice and check if user exist in userRepository.
			URL url = new URL("http://manas-user-service:8080/users/"+user_id);
			
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        connection.setDoOutput(true);
			connection.setRequestMethod("GET");
	        
	        int responseCode = connection.getResponseCode();
			
			if(responseCode!=HttpURLConnection.HTTP_OK) {
// Return NOTFOUND if user doesn't exist.
				return new ResponseEntity<>("User Doesn't Exist", HttpStatus.NOT_FOUND);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		try {
// bookings List contains all booking details
			List<Booking> bookings = bookingService.getAllBooking();
			if(bookings.size()==0) {
// Executed if  bookingRepository doesn't have any booking.
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}else {
// amount stores amount to be added in users wallet after deleting corresponding 
				int amount=0;
				Show show = new Show();
				int count=0;
				for(int i=0; i<bookings.size(); i++) {
// Iterating through each booking checking if its user_id matches input user_id
					if(bookings.get(i).getUser_id()==user_id) {
// if booking under consideration is done by our user 
// we checking for no. of seats booked and there price in show of this particular booking 
// add amount gained  by deleting this booking to amount variable 
						show = bookingService.getShowById(bookings.get(i).getShow_id());
						amount += (show.getPrice()*bookings.get(i).getSeats_booked()); 
// update seats in corresponding shows					
						show.setSeats_available(show.getSeats_available()+bookings.get(i).getSeats_booked());
						
						bookingService.updateSeats(show);
// delete this booking
						bookingService.deleteBookingByBookingId(bookings.get(i).getId());
						count++;
					}
					
				}
				if(count==0) {
// Executed if user doesn't have any booking 
					return new ResponseEntity<String>("No bookings",HttpStatus.NOT_FOUND);
				}
				try {
//  Establishes connection with WALLET microservice to update amount in users wallet
					URL url = new URL("http://manas-wallet-service:8080/wallets/"+user_id);
					
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			        connection.setDoOutput(true);
					connection.setRequestMethod("PUT");
			        connection.setRequestProperty("Content-Type", "application/json");
// send request to update amount
			        String payload = "{ \"action\":\"credit\" ,\"amount\": "+amount+"}";
			        
			        try(OutputStream os = connection.getOutputStream()) {
			            byte[] input = payload.getBytes("utf-8");
			            os.write(input, 0, input.length);
			            os.flush();
			        }
			        int responseCode = connection.getResponseCode();
					
					if(responseCode!=HttpURLConnection.HTTP_OK) {
						
					}
						
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK );
			}
			
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
	}
	
	@DeleteMapping("/bookings")
// No input 
// No output 
	public ResponseEntity<?> deleteAllBooking(){
		List<Booking> bookings = bookingService.getAllBooking();
		Show show = new Show();
		int amount=0;
// Iterating over booking List
		for(int i=0 ; i<bookings.size(); i++) {

			show = bookingService.getShowById(bookings.get(i).getShow_id());
// amount store amount to be send in corresponding users acount after deleting this booking.
			amount = (bookings.get(i).getSeats_booked())*(show.getPrice());
// updating no. of seats for corresponding show, this will be updated later just before this iteration of for loop terminates. 
			show.setSeats_available(show.getSeats_available()+bookings.get(i).getSeats_booked());

			try {
// Establish connection with WALLET microservice to update balance in wallet of corresponding user. 
				URL url = new URL("http://manas-wallet-service:8080/wallets/"+bookings.get(i).getUser_id());
				
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("PUT");
		        connection.setRequestProperty("Content-Type", "application/json");
		        connection.setDoOutput(true);

// Update balance in corresponding users account.
		        String payload = "{ \"action\":\"credit\",\"amount\":"+amount+"}";
		        
		        try(OutputStream os = connection.getOutputStream()) {
		            byte[] input = payload.getBytes("utf-8");
		            os.write(input, 0, input.length);
		            os.flush();
		        }
		        
			    int responsecode = connection.getResponseCode();
			    if(responsecode != HttpURLConnection.HTTP_OK) {
			    	throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			    }

					
			} catch (Exception e) {
				e.printStackTrace();
			}
// Updates show state of show of booking under consideration in this iteration of for loop
			bookingService.updateSeats(show);
			
		}
// Deletes all booking
		bookingService.deleteAllBooking();
		return new ResponseEntity<>("All Bookings Deleted", HttpStatus.OK);
		
	}
	
	
	
}























