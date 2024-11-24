package com.subhash.service;

import com.subhash.model.TwoFactorOtp;
import com.subhash.model.User;
import com.subhash.repository.TwoFactorOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class TwoFactorOtpService {

    @Autowired
    private TwoFactorOtpRepository repository;

    public TwoFactorOtp createTwoFactorOtp(User user, String otp, String jwt){

        UUID uuid=UUID.randomUUID();
        String id=uuid.toString();
        TwoFactorOtp twoFactorOtp=new TwoFactorOtp();
        twoFactorOtp.setOtp(otp);
        twoFactorOtp.setId(id);
        twoFactorOtp.setJwt(jwt);
        twoFactorOtp.setUser(user);
        return repository.save(twoFactorOtp);
    }

    public TwoFactorOtp findByUser(Long userId){
        return repository.findByUserId(userId);
    }

    public TwoFactorOtp findById(String id){
        Optional<TwoFactorOtp> otp=repository.findById(id);
        return otp.orElse(null);
    }

    public boolean verifyTwoFactorOtp(TwoFactorOtp twoFactorOtp,String otp){
        return twoFactorOtp.getOtp().equals(otp);
    }

    public void deleteTwoFactorOtp(TwoFactorOtp twoFactorOtp){
        repository.delete(twoFactorOtp);
    }
}