package com.PODS_Project.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.PODS_Project.repository.UserRepository;
import com.PODS_Project.entity.User;

@Service
public class UserService {
    @Autowired
	private UserRepository userRepository;
    
    public User createUser(User user) {
    	return userRepository.save(user);
    }
    public User getUserById(int id) {
		return userRepository.findById(id).orElse(null);
	}
	public String deleteUserById(int id) {
		userRepository.deleteById(id);
		return "Deleted Succesfully";
	}
	public String deleteUsers() {
		userRepository.deleteAll();
		return "All Users Deleted";
	}
		

}
