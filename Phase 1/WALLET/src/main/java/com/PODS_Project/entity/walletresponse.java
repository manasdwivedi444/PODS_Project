package com.PODS_Project.entity;

public class walletresponse {
   int user_id;
   int balance;
   public walletresponse() {
	   
   }
public walletresponse(int user_id, int balance) {
	super();
	this.user_id = user_id;
	this.balance = balance;
}
public int getUser_id() {
	return user_id;
}
public void setUser_id(int user_id) {
	this.user_id = user_id;
}
public int getBalance() {
	return balance;
}
public void setBalance(int balance) {
	this.balance = balance;
}
   
}
