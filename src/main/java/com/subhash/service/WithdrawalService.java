package com.subhash.service;

import com.subhash.enums.WithdrawalStatus;
import com.subhash.model.User;
import com.subhash.model.Withdrawal;
import com.subhash.repository.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    public Withdrawal requestWithdrawal(Long amount, User user){
        Withdrawal withdrawal=new Withdrawal();
        withdrawal.setAmount(amount);
        withdrawal.setUser(user);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        return withdrawalRepository.save(withdrawal);
    }

    public Withdrawal proceedWithWithdrawal(Long withdrawalId,boolean accept){
        Optional<Withdrawal> withdrawal=withdrawalRepository.findById(withdrawalId);
        if(withdrawal.isEmpty()){
            throw new RuntimeException("withdrawal not found");
        }
        Withdrawal withdrawal1=withdrawal.get();
        withdrawal1.setDate(LocalDateTime.now());
        if(accept){
            withdrawal1.setStatus(WithdrawalStatus.SUCCESS);
        }
        else{
            withdrawal1.setStatus(WithdrawalStatus.PENDING);
        }
        return withdrawalRepository.save(withdrawal1);
    }

    public List<Withdrawal> getUsersWithdrawalHistory(User user){
        return withdrawalRepository.findByUserId(user.getId());
    }

    public List<Withdrawal>getAllWithdrawalRequest(){
        return withdrawalRepository.findAll();
    }
}

