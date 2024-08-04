package com.userservice.application.user.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userservice.application.user.repositories.UserRepository;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void clearUserData(int userId) {
		//send HTTP request to booking service to delete all the bookings of user with userId
		try {
			URL url = new URL("http://host.docker.internal:8081/bookings/users/"+userId);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");
			int code = con.getResponseCode();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		//send HTTP request to wallet service to delete the wallet of user with userId
		try {
			URL url = new URL("http://host.docker.internal:8082/wallets/"+userId);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");
			int code = con.getResponseCode();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
}
