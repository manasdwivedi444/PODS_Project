package com.walletservice.application.wallet.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;

//This class is used as request body for PUT requests
@Getter@NoArgsConstructor
public class WalletUpdateRequest {
    private String action;
    private int amount;

    @Override
    public String toString(){
        return "WalletUpdateRequest string {"+
				" action = " + action +
				", amount = " + amount +
				" }";
    }
}
