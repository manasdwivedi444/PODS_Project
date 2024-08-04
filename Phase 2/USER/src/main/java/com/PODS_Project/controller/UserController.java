package com.PODS_Project.controller;



import java.net.HttpURLConnection;
import java.net.URL;

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

import com.PODS_Project.entity.User;
import com.PODS_Project.service.UserService;

@Transactional(isolation = Isolation.SERIALIZABLE)
@RestController
public class UserController {
	@Autowired
	private UserService userService;
	
	@PostMapping("/users")    
    // Takes an JSON input from request body, which contains name and email of user to be created.
	// Returns a JSON output containing Id , name and email of newly created user (if user creation successful).
	public ResponseEntity<?> addUser(@RequestBody User user) {
		try {
			     
				 return new ResponseEntity<>( userService.createUser(user),HttpStatus.CREATED );
			 
		}
		catch (Exception e) {
	// Will be executed if any error occurs in adding new user, like email already exist in userRepository.
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/users/{id}")    
	// Takes input user_id as a PathVariable.
	// Returns JSON output containing Id , name and email of user with Id equal to user_id (if user exist).
	public ResponseEntity<?> getUserById(@PathVariable("id") int id) {
		if(userService.getUserById(id)==null){
	// Will be executed if userRepository does not have any user with Id equal to user_id.
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		else {
	// Will return JSON output containing Id , name and email of user with Id equal to user_id.
			return new ResponseEntity<>( userService.getUserById(id),HttpStatus.OK );
		}
	}

	@DeleteMapping("/users/{id}")
	// Takes input user_id as a PathVariable.
	public ResponseEntity<?> deleteUserById(@PathVariable("id") int id) {
		if(userService.getUserById(id)==null) {
	// Will be executed if userRepository does not have any user with Id equal to user_id.
			return new ResponseEntity<>("User Doesn't Exist",HttpStatus.NOT_FOUND);
		}else {
	
    
	// Below try and catch pair deletes bookings with user_id(coulmn) equal to user_id(Path_Variable).
			try {
	// Establishes connection with bookingRepository and making request to delete bookings of user with Id equal to user_id.
				URL url = new URL("http://manas-booking-service:8080/bookings/users/"+id);  
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("DELETE");
				int responseCode = connection.getResponseCode();
	// Below condition will be true only if User does not exist
	// but if this is case, code execution will never reach this point because of earlier if statement.
				if(responseCode!=HttpURLConnection.HTTP_OK) {
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

    // Below try and catch pair deletes wallet with Id equal to user_id.
			try {
    // Establishes connection with walletRepository and making request to delete wallet of user with Id equal to user_id.
				URL url = new URL("http://manas-wallet-service:8080/wallets/"+id); 
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("DELETE");
				int responseCode = connection.getResponseCode();
	// Below condition will be true only if 
	// 1. User does not exist
	// 2. wallet does not exist			
	// if 1st is situation, code execution will never reach this point because of earlier if statement.
	// if 2nd is situation, we don't have to do anything as wallet user with Id equal to user_id already doesn't exist.
				if(responseCode!=HttpURLConnection.HTTP_OK) {
					
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

	// Delete user from userRepository		
			userService.deleteUserById(id);
	
			return new ResponseEntity<>("User Deleted Successfully",HttpStatus.OK);
		}
		
		
	}
	

	@DeleteMapping("/users")
	// No JSON input , No PathVariable input
	// Will always return response code 200(OK)
	public ResponseEntity<?> deleteUsers() {
	// deletes all users from userRepository 
		userService.deleteUsers();
	// connects with WALLET microservice and deletes all wallets from walletRepository(as there shouln't be any wallet if there is no users)
		try {
			URL url = new URL("http://manas-wallet-service:8080/wallets");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("DELETE");
			int responseCode = connection.getResponseCode();
			
			if(responseCode!=HttpURLConnection.HTTP_OK) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	// connects with BOOKING microservice and deletes all booking from bookingRepository(as there shouln't be any booking if there is no users)
		try {
			URL url = new URL("http://manas-booking-service:8080/bookings");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("DELETE");
			int responseCode = connection.getResponseCode();
			
			if(responseCode!=HttpURLConnection.HTTP_OK) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("All Users Deleted", HttpStatus.OK);
	}
}
