package com.PODS_Project.entity;




public class Transactions {

	private String action;
	private int amount;
	
    public Transactions(String action, int amount){
        this.action = action ;
        this.amount = amount ;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

