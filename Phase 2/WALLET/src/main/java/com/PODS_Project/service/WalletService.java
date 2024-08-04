package com.PODS_Project.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.PODS_Project.entity.Wallet;
import com.PODS_Project.repository.WalletRepository;

@Service
public class WalletService {
	@Autowired
	private WalletRepository walletRepository;
	
	public Wallet getWalletById(int id) {
		return walletRepository.findById(id).orElse(null);
	}
	public Wallet updateWallet(Wallet wallet) {
		return walletRepository.save(wallet);
	
	}
	public void deleteWalletById(int id) {
		walletRepository.deleteById(id);
	}
	
	public String deleteWallets() {
		walletRepository.deleteAll();
		return "All Wallets Deleted";
	}
	
}
