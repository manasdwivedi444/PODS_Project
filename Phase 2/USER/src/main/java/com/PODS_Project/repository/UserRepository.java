package com.PODS_Project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.PODS_Project.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}
