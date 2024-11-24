package com.subhash.service;

import com.subhash.enums.OrderType;
import com.subhash.model.Order;
import com.subhash.model.User;
import com.subhash.model.Wallet;
import com.subhash.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public Wallet getUserWallet(User user){
        Wallet wallet=walletRepository.findByUserId(user.getId());
        if(wallet== null){
            wallet=new Wallet();
            wallet.setUser(user);
            walletRepository.save(wallet);
        }
        return wallet;
    }

    public Wallet addBalance(Wallet wallet,Long money){
        BigDecimal balance=wallet.getBalance();
        BigDecimal newBalance=balance.add(BigDecimal.valueOf(money));
        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }

    public Wallet findWalletById(Long id){
        Optional<Wallet> wallet=walletRepository.findById(id);
        if(wallet.isPresent()){
            return wallet.get();
        }
        throw new RuntimeException("Wallet not found");
    }

    public Wallet walletToWalletTransfer(User sender,Wallet receiverWallet,Long amount){
        Wallet senderWallet=getUserWallet(sender);
        if(senderWallet.getBalance().compareTo(BigDecimal.valueOf(amount))<0){
            throw new RuntimeException("insufficient balance");
        }
        BigDecimal senderBalance=senderWallet.getBalance().subtract(BigDecimal.valueOf(amount));
        senderWallet.setBalance(senderBalance);
        walletRepository.save(senderWallet);

        BigDecimal receiverBalance=receiverWallet.getBalance().add(BigDecimal.valueOf(amount));
        receiverWallet.setBalance(receiverBalance);
        walletRepository.save(receiverWallet);
        return senderWallet;
    }

    public Wallet payOrderPayment(Order order, User user){
        Wallet wallet=getUserWallet(user);
        if(order.getOrderType().equals(OrderType.BUY)){
            BigDecimal newBalance=wallet.getBalance().subtract(order.getPrice());
            if(newBalance.compareTo(order.getPrice())<0){
                throw new RuntimeException("Insufficient funds for this transaction");
            }
            wallet.setBalance(newBalance);
        }else{
            BigDecimal newBalance=wallet.getBalance().add(order.getPrice());
            wallet.setBalance(newBalance);
        }
        return walletRepository.save(wallet);
    }
}
