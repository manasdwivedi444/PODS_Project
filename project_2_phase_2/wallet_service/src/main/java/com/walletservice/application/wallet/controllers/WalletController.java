package com.walletservice.application.wallet.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.walletservice.application.wallet.entities.WalletUpdateRequest;
import com.walletservice.application.wallet.repositories.WalletRepository;
import com.walletservice.application.wallet.services.WalletService;

@RestController 
@RequestMapping("wallets")
public class WalletController {

    @Autowired
    private WalletService walletservice;

    @Autowired
    private WalletRepository walletRepository;

    //Get wallet of user with the userId
    @GetMapping(path="{user_id}")
    public ResponseEntity<?> getByUserid(@PathVariable Integer user_id){
            return walletservice.getByUserid(user_id);
    }    

    //Update wallet of given user with given amount for given type of transaction
    @PutMapping(path="{user_id}")
    public ResponseEntity<?> updateWalletBalance(@PathVariable Integer user_id,
                                                @RequestBody WalletUpdateRequest request){
            try{
            return walletservice.updateWalletBalance(user_id,request);}
            catch(Exception e){
                System.out.println(e);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

    }

    //Delete wallet with given userId
    @DeleteMapping(path="{user_id}")
    public ResponseEntity<?> delByUserid(@PathVariable Integer user_id){
            return walletservice.delByUserid(user_id);
    }    

    //Delete all wallets
    @DeleteMapping()
    public ResponseEntity<?> delAll(){
        walletRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }



        
}
