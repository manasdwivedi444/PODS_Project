package com.userservice.application.user.services;

import jakarta.transaction.Transactional;

//UserServiceImpl class implements this interface
public interface UserService {
	
	public void clearUserData(int userId);
	
}
