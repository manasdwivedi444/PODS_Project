package com.walletservice.application.wallet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter@Setter@NoArgsConstructor@AllArgsConstructor
public class Wallet {

	@Id
	@Column(name="user_id")
	private Integer user_id;
	
	@Column(name="balance", nullable=false)
	private int balance;

	@Override
    public String toString(){
        return "Wallet string {"+
				" user_id = " + user_id +
				", balance = " + balance +
				" }";
    }
	
}
