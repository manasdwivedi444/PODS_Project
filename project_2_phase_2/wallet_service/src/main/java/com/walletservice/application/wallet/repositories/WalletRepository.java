package com.walletservice.application.wallet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.walletservice.application.wallet.entities.Wallet;

@Repository
public interface WalletRepository
 extends JpaRepository<Wallet, Integer> {
    
}
