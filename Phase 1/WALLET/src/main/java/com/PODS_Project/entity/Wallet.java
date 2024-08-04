package com.PODS_Project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
public class Wallet {
	@Id
	@Column(name = "user_id")
	private int user_id;
	@Column
	private int balance;
	
	public Wallet() {
		
	}
	public Wallet(int user_id  , int balance){
		this.balance = balance;
		this.user_id = user_id;
	}
	
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
	public int getId() {
		return user_id;
	}
	public void setId(int user_id) {
		this.user_id = user_id;
	}
	
	

}