package com.PODS_Project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PODS_Project.entity.Wallet;


public interface WalletRepository extends JpaRepository<Wallet, Integer> {

}
