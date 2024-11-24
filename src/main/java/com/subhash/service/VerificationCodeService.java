package com.subhash.service;

import com.subhash.enums.VerificationType;
import com.subhash.model.User;
import com.subhash.model.VerificationCode;
import com.subhash.repository.VerificationCodeRepository;
import com.subhash.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class VerificationCodeService {

    @Autowired
    private VerificationCodeRepository repository;

    public VerificationCode sendVerificationCode(User user, VerificationType verificationType){
        VerificationCode verificationCode1=new VerificationCode();
        verificationCode1.setOtp(OtpUtils.generateOtp());
        verificationCode1.setVerificationType(verificationType);
        verificationCode1.setUser(user);
        return  repository.save(verificationCode1);
    }

    public VerificationCode getVerificationCodeById(Long id){
        Optional<VerificationCode> verificationCode=repository.findById(id);
        if(verificationCode.isPresent()){
            return verificationCode.get();
        }
        else{
            throw new RuntimeException("Verification code not found");
        }
    }

    public VerificationCode getVerificationCodeByUser(Long userId){
        return repository.findByUserId(userId);
    }

    public void deleteVerificationCodeById(VerificationCode verificationCode){
        repository.delete(verificationCode);
    }
}

