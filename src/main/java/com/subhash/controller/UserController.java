package com.subhash.controller;

import com.subhash.enums.VerificationType;
import com.subhash.model.ForgotPasswordToken;
import com.subhash.model.User;
import com.subhash.model.VerificationCode;
import com.subhash.request.ForgotPasswordTokenRequest;
import com.subhash.request.ResetPasswordRequest;
import com.subhash.response.ApiResponse;
import com.subhash.response.LoginResponse;
import com.subhash.service.ForgotPasswordService;
import com.subhash.service.UserService;
import com.subhash.service.VerificationCodeService;
import com.subhash.utils.EmailService;
import com.subhash.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;


    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt){
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        System.out.println(jwt);
        User user=userService.findUserProfileByJwt(jwt.trim());
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @PostMapping("/api/users/verification/{verificationType}/send-otp")
    public ResponseEntity<String>sendVerificationOtp(@RequestHeader("Authorization") String jwt,
                                                     @PathVariable VerificationType verificationType){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        VerificationCode verificationCode=verificationCodeService.getVerificationCodeByUser(user.getId());

        if(verificationCode == null){
            verificationCode=verificationCodeService.sendVerificationCode(user,verificationType);
        }

        if(verificationType.equals(VerificationType.EMAIL)){
            emailService.sendVerificationOtp(user.getEmail(),verificationCode.getOtp());
        }
        return new ResponseEntity<>("Otp successfully sent",HttpStatus.OK);
    }

    @PatchMapping("/api/users/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<User>enableTwoFactorAuthentication(@RequestHeader("Authorization") String jwt,
                                                             @PathVariable String otp){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        VerificationCode verificationCode=verificationCodeService.getVerificationCodeByUser(user.getId());

        String sendTo=verificationCode.getVerificationType().equals(VerificationType.EMAIL)?
                verificationCode.getEmail():verificationCode.getMobile();

        boolean isVerified=verificationCode.getOtp().equals(otp);

        if(isVerified){
            User updatedUser=userService.enableTwoFactorAuthentication(verificationCode.getVerificationType(),sendTo,user);
            verificationCodeService.deleteVerificationCodeById(verificationCode);
            return new ResponseEntity<>(updatedUser,HttpStatus.OK);
        }
        throw new RuntimeException("Wrong Otp");
    }

    @PostMapping("api/auth/users/reset-password/send-otp")
    public ResponseEntity<LoginResponse>sendForgotPasswordOtp(@RequestBody ForgotPasswordTokenRequest request){

        User user=userService.findUserByEmail(request.getSendTo());
        String otp= OtpUtils.generateOtp();
        UUID uuid=UUID.randomUUID();
        String id=uuid.toString();

        ForgotPasswordToken token=forgotPasswordService.findByUser(user.getId());
        if(token == null){
            token=forgotPasswordService.createToken(user,id,otp,request.getVerificationType(), request.getSendTo());
        }
        if(request.getVerificationType().equals(VerificationType.EMAIL)){
            emailService.sendVerificationOtp(user.getEmail(),token.getOtp());
        }

        LoginResponse response=new LoginResponse();
        response.setSession(token.getId());
        response.setMessage("Password reset otp send successfully");

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("api/auth/users/reset-password/verify-otp")
    public ResponseEntity<ApiResponse>resetPassword(@RequestParam String id,
                                                    @RequestBody ResetPasswordRequest request){
        ForgotPasswordToken forgotPasswordToken=forgotPasswordService.findById(id);
        boolean isVerified=forgotPasswordToken.getOtp().equals(request.getOtp());

        if(isVerified){
            userService.updatePassword(forgotPasswordToken.getUser(),request.getPassword());
            ApiResponse response=new ApiResponse();
            response.setMessage("Password update Successfully");
            return new ResponseEntity<>(response,HttpStatus.ACCEPTED);
        }
        throw new RuntimeException("Wrong otp");
    }
}
