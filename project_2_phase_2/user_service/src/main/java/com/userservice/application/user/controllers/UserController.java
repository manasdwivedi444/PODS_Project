package com.userservice.application.user.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userservice.application.user.entites.User;
import com.userservice.application.user.repositories.UserRepository;
import com.userservice.application.user.services.UserService;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@RestController
@RequestMapping(value="/users")
public class UserController {
	
	@Autowired 
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;
	
	//create a new user
	@Transactional(isolation = Isolation.SERIALIZABLE)
	@PostMapping(value="", consumes = "application/json")
	public ResponseEntity<?> addNewUser(@RequestBody User user){
		//If a User already exists with given email then send bad request as response
		//Create user only if the no user has the given email
		if( userRepository.existsByEmail(user.getEmail()) ) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		else {
			User newUser = userRepository.save(user);
			return new ResponseEntity<>(newUser, HttpStatus.CREATED);
		}
	}

	//send user with given userId
	@Transactional(isolation = Isolation.SERIALIZABLE)
	@GetMapping(value="/{userId}")
	public ResponseEntity<?> getUser(@PathVariable int userId){
		Optional<User> searchedUser = userRepository.findById(userId);
		if(searchedUser.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		else {
			return new ResponseEntity<>(searchedUser, HttpStatus.OK);
		}
	}
	
	//delete user with given userId along with its associated wallet and bookings
	@Transactional(isolation = Isolation.SERIALIZABLE)
	@DeleteMapping(value="/{userId}")
	public ResponseEntity<?> deleteUser(@PathVariable int userId){
		if(userRepository.existsById(userId)) {
			userRepository.deleteById(userId);
			userService.clearUserData(userId);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	//delete all users along with its associated wallet and bookings
	@Transactional(isolation = Isolation.SERIALIZABLE)
	@DeleteMapping(value="")
	public ResponseEntity<?> deleteAllUsers(){
		List<User> users = userRepository.findAll();
		//delete bookings and wallet of every user
		for(User user:users) {
			userService.clearUserData(user.getId());
		}
		userRepository.deleteAll();
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
