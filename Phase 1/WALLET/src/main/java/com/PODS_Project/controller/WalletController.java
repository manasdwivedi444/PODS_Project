package com.PODS_Project.controller;

import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.PODS_Project.entity.Transactions;
import com.PODS_Project.entity.Wallet;
import com.PODS_Project.entity.walletresponse;
import com.PODS_Project.service.WalletService;

@RestController
public class WalletController {
	@Autowired
	private WalletService walletService;

	@GetMapping("/wallets/{user_id}")
	// Takes PathVariable user_id as input.
	// Return JSON output containig Id (equal to user_id) and balance (if User exist).
	public ResponseEntity<?> getWalletById(@PathVariable("user_id") int user_id) {
		Wallet w1 = walletService.getWalletById(user_id);
		if(w1==null) {
	// Will be executed if no wallet with Id equal to user_id or user does not exist(as non existense of user imply non existense of wallet).
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}else {
	// Below lines is to make response entity which gives output in desired format
		    walletresponse w2 = new walletresponse();
		    w2.setUser_id(w1.getId());
		    w2.setBalance(w1.getBalance());
			
			return new ResponseEntity<>(w2, HttpStatus.OK);
		}
	}
	// Takes PathVariable user_id as input and JSON body as input.
	// adds/substract (depending on "action" attribute value in JSON ) amount in wallet of user (if user exist).
	// except when balance in account is less than amount in case of debit.
	// returns updated value wallet as JSON output.
	@PutMapping("/wallets/{user_id}")
    public ResponseEntity<?> updateBalance(@PathVariable("user_id") int user_id, @RequestBody Transactions trans){

        try{
              
	            Wallet wallet = walletService.getWalletById(user_id);
	            int balance=0;
	           
	            if (wallet == null) {
	// Will be executed if no wallet in walletRepository for user with Id equal to user_id.
	                try {
	// Below 4 lines establishes connection with USER microservice and check if user exist in userRepository.
	    				URL url = new URL("http://host.docker.internal:8080/users/"+user_id); /* here i am testing */
	    				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    				connection.setRequestMethod("GET");
	    				int responseCode = connection.getResponseCode();
	    				
	    				if (responseCode == HttpURLConnection.HTTP_OK) {
	// Will be executed if user exist in userRepository but wallet doesn't exist 
	// It creates a new zero balance account for user
	    					 wallet = new Wallet(user_id,0) ;
	    					 walletService.updateWallet(wallet);
	    					 
	    				}else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
	// Will be executed if user doesn't exist in userRepository
	    					 return new ResponseEntity<>("No User with this ID", HttpStatus.NOT_FOUND);
	    				}
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
	            }else {
	            
	            balance = wallet.getBalance();
	            }
	            
	            if (trans.getAction().equalsIgnoreCase("credit")){
	// Will be executed if "action" attribute in input JSON is credit (case of letters doesn't matter)
	                 wallet.setBalance(balance + trans.getAmount());
	                 walletService.updateWallet(wallet);
	
	            }else if(trans.getAction().equalsIgnoreCase("debit")){
	// Will be executed if "action" attribute in input JSON is debit (case of letters doesn't matter)            	
	                 if (trans.getAmount() > balance){
	// Will be execiuted if amount to be debited is more than balance in account (invalid debit request (in loose language))
	                     return new ResponseEntity<>( "Insufficient Balance", HttpStatus.BAD_REQUEST);
	                     
	                 }else{
	// Update balaqnce in case valid debit request        	 
	                	 wallet.setBalance(balance - trans.getAmount());
	                	 walletService.updateWallet(wallet);
	                 }
	            }     
	            walletresponse w = new walletresponse();
	            w.setUser_id(wallet.getId());
	            w.setBalance(wallet.getBalance());
	// Return JSON output containing user_id and updated balance 
                return new ResponseEntity<>(w, HttpStatus.OK);

            
        }catch (Exception e){
        	e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }
	
	@DeleteMapping("/wallets/{user_id}")
	// Takes a PathVariable user_id as input

	public ResponseEntity<?> deleteWalletById(@PathVariable("user_id") int user_id) {
		if(walletService.getWalletById(user_id)==null) {
	// Executed if user doesn't have wallet or user doesn't exist(as non existense of user imply non existense of wallet).
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}else {
	// Executed if wallet for user exist, it deletes that wallet.
			walletService.deleteWalletById(user_id);
			return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);
		}
	}
	
	@DeleteMapping("/wallets")
	// No input, No output (except HttpStatusCode)
	public String deleteWallets() {
	// Deletes all wallets
		return walletService.deleteWallets();
	}
	
	
}
