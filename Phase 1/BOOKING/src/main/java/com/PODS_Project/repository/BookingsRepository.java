package com.PODS_Project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PODS_Project.entity.Booking;

public interface BookingsRepository extends JpaRepository<Booking, Integer>{

}
