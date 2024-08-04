package com.userservice.application.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.userservice.application.user.entites.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
	@Query(value = """
	        select count(u.id) = 1
	        from User u
	        where u.email = :email
	        """
	    )
	//Check if user exists with given email
	    boolean existsByEmail(@Param("email") String email);
	
}
