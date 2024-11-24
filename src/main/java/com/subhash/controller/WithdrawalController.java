package com.subhash.controller;

import com.subhash.model.User;
import com.subhash.model.Wallet;
import com.subhash.model.Withdrawal;
import com.subhash.service.UserService;
import com.subhash.service.WalletService;
import com.subhash.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    //This Withdrawal controller is to withdraw the amount that we have in our account/wallet

    @PostMapping("/api/withdrawal/{amount}")
    public ResponseEntity<?> withdrawalRequest(@PathVariable Long amount, @RequestHeader("Authorization") String jwt){

        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Wallet userWallet=walletService.getUserWallet(user);
        Withdrawal withdrawal=withdrawalService.requestWithdrawal(amount,user);
        walletService.addBalance(userWallet,-withdrawal.getAmount());
        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }

    @PatchMapping("/api/admin/withdrawal/{id}/proceed/{accept}")
    public ResponseEntity<?>proceedWithdrawal(@PathVariable Long id,@PathVariable boolean accept,
                                              @RequestHeader("Authorization")String jwt){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Withdrawal withdrawal=withdrawalService.proceedWithWithdrawal(id,accept);
        Wallet userWallet=walletService.getUserWallet(user);
        if(!accept){
            walletService.addBalance(userWallet,withdrawal.getAmount());
        }
        return new ResponseEntity<>(withdrawal,HttpStatus.OK);
    }

    @GetMapping("/api/withdrawal")
    public ResponseEntity<List<Withdrawal>>getWithdrawalHistory(@RequestHeader("Authorization")String jwt){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        List<Withdrawal>withdrawals=withdrawalService.getUsersWithdrawalHistory(user);
        return new ResponseEntity<>(withdrawals, HttpStatus.OK);
    }

    @GetMapping("/api/admin/withdrawal")
    public ResponseEntity<List<Withdrawal>>getAllWithdrawalRequest(@RequestHeader("Authorization")String jwt){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        List<Withdrawal>withdrawals=withdrawalService.getAllWithdrawalRequest();
        return new ResponseEntity<>(withdrawals, HttpStatus.OK);
    }
}

