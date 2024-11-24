package com.subhash.service;

import com.subhash.enums.VerificationType;
import com.subhash.model.ForgotPasswordToken;
import com.subhash.model.User;
import com.subhash.repository.ForgotPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ForgotPasswordService {

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    public ForgotPasswordToken createToken(User user, String id, String otp,
                                           VerificationType verificationType, String sendTo){
        ForgotPasswordToken token=new ForgotPasswordToken();
        token.setUser(user);
        token.setSendTo(sendTo);
        token.setVerificationType(verificationType);
        token.setOtp(otp);
        token.setId(id);
        return forgotPasswordRepository.save(token);
    }

    public ForgotPasswordToken findById(String id){
        Optional<ForgotPasswordToken> token=forgotPasswordRepository.findById(id);
        return token.orElse(null);
    }

    public ForgotPasswordToken findByUser(Long userId) {
        return forgotPasswordRepository.findByUserId(userId);
    }

    public void deleteToken(ForgotPasswordToken token){
        forgotPasswordRepository.delete(token);
    }
}

