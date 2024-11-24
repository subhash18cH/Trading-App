package com.subhash.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendVerificationOtp(String email, String otp){
        SimpleMailMessage message=new SimpleMailMessage();
        message.setSubject("Verify OTP");
        message.setTo(email);
        message.setText("Your Verification code is "+otp);
        javaMailSender.send(message);
    }

}
