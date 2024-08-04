package com.PODS_Project.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.PODS_Project.entity.Booking;
import com.PODS_Project.entity.Show;
import com.PODS_Project.entity.Theatre;
import com.PODS_Project.repository.BookingsRepository;
import com.PODS_Project.repository.ShowRepository;
import com.PODS_Project.repository.TheatreRepository;


@Service
public class BookingService {
	@Autowired
	private BookingsRepository bookingRepository;
	@Autowired
	private TheatreRepository theatreRepository;
	@Autowired
	private ShowRepository showRepository;
	
	
	
	public Theatre getTheatreById(int id) {
		return theatreRepository.findById(id).orElse(null);
	}
	
	public List<Theatre> getAllTheatres(){
		return theatreRepository.findAll();
	}
	
	public List<Show> getAllShow(){
		return showRepository.findAll();
	}
	
	public Show getShowById(int id) {
		return showRepository.findById(id).orElse(null);
	}
	
	public List<Booking> getBookingByUserId(int id){
		List<Integer> bookings = Arrays.asList(id);
		return bookingRepository.findAllById(bookings);
	}
	
	public void bookMyShow(Booking booking) {
		bookingRepository.save(booking);
	}
	
	public void deleteBookingByBookingId(int id) {
		bookingRepository.deleteById(id);
	}
	
	public void deleteAllBooking() {
		bookingRepository.deleteAll();
	}
	
	public void updateSeats(Show show) {
		showRepository.save(show);
	}

	public void saveShowData() {
		try (BufferedReader b = new BufferedReader(new FileReader("src/main/resources/shows.csv"))) {
			String s;
			Show show = new Show();
			b.readLine();
			while((s=b.readLine())!=null) {
				String []data = s.split(",");
				show.setId(Integer.parseInt(data[0]));
				show.setTheatre_id(Integer.parseInt(data[1]));
				show.setTitle(data[2]);
				show.setPrice(Integer.parseInt(data[3]));
				show.setSeats_available(Integer.parseInt(data[4]));
				
				showRepository.save(show);
			}
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveTheatreData() {
		try (BufferedReader b = new BufferedReader(new FileReader("src/main/resources/theatres.csv"))) {
			String s;
			Theatre t = new Theatre();
			b.readLine();
			while((s=b.readLine())!=null) {
				String []data = s.split(",");
				t.setId(Integer.parseInt(data[0]));
				t.setName(data[1]);
				t.setLocation(data[2]);
				
					theatreRepository.save(t);
				
			}
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Booking> getAllBooking(){
		return bookingRepository.findAll();
	} 
	
	
}

































