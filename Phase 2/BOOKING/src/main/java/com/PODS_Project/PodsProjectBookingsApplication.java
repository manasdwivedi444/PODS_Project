package com.PODS_Project;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.PODS_Project.service.BookingService;



@SpringBootApplication
public class PodsProjectBookingsApplication implements CommandLineRunner {

	@Autowired
	private BookingService bookingService;
	public static void main(String[] args) {
				SpringApplication.run(PodsProjectBookingsApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception{
	       bookingService.saveShowData();
	       bookingService.saveTheatreData();
	}

}
