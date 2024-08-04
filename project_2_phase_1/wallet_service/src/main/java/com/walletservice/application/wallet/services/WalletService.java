package com.walletservice.application.wallet.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.walletservice.application.wallet.entities.Wallet;
import com.walletservice.application.wallet.entities.WalletUpdateRequest;
import com.walletservice.application.wallet.repositories.WalletRepository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

//This class contains all the business logic all operations related to wallet entity
@Service
public class WalletService {

	@Autowired
    private WalletRepository walletRepository;

	//Return wallet with given userId
	@Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<?> getByUserid(Integer user_id){
        Optional<Wallet> walletOptional = walletRepository.findById(user_id);

        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        } 
        //Send not found if user doesn't have any wallet
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //delete wallet of user with given userId
	@Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<?> delByUserid(Integer user_id){
        Optional<Wallet> walletOptional = walletRepository.findById(user_id);
        
        if (walletOptional.isPresent()) {
            walletRepository.deleteById(user_id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
      //Send not found if user doesn't have any wallet
        else {
            return new ResponseEntity<>("Wallet not found", HttpStatus.NOT_FOUND);
        }
    }

    //Update the balance of the balance
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    @Retryable(backoff = @Backoff(delay=50, maxDelay = 500), maxAttempts = 15)
    public ResponseEntity<?> updateWalletBalance(Integer user_id,
                                                WalletUpdateRequest request) throws Exception {
        Optional<Wallet> walletOptional = walletRepository.findById(user_id);
        //System.out.println("Readwallet");
        Wallet wallet = walletOptional.orElseGet(() -> createNewWallet(user_id));
        //System.out.println(wallet.toString());
        walletRepository.flush();
        //If the action is Debit
        if ("debit".equals(request.getAction())) {
            int currentBalance = wallet.getBalance();
            int amount = request.getAmount();
            //For insufficient Balance
            if (currentBalance < amount) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            wallet.setBalance(currentBalance - amount);
        }
        //If action is credit
        else {
            int currentBalance = wallet.getBalance();
            int amount = request.getAmount();
            wallet.setBalance(currentBalance + amount);
        }
        //Save the wallet with updated balance
        walletRepository.saveAndFlush(wallet);
        //System.out.println("Savewallet");
        //System.out.println(wallet.toString());
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }
    
    //If user doesn't has wallet then create a new wallet
    private Wallet createNewWallet(Integer user_id) {
        Wallet newWallet = new Wallet();
        newWallet.setUser_id(user_id);
        newWallet.setBalance(0);
        walletRepository.saveAndFlush(newWallet);
        return newWallet;
    }

    
}
