package com.subhash.controller;

import com.subhash.model.TwoFactorOtp;
import com.subhash.model.User;
import com.subhash.repository.UserRepository;
import com.subhash.request.LoginRequest;
import com.subhash.request.SignUpRequest;
import com.subhash.response.LoginResponse;
import com.subhash.security.jwt.JwtUtils;
import com.subhash.security.service.MyUserDetails;
import com.subhash.service.TwoFactorOtpService;
import com.subhash.service.UserService;
import com.subhash.service.WatchListService;
import com.subhash.utils.EmailService;
import com.subhash.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private TwoFactorOtpService twoFactorOtpService;

    @Autowired
    private WatchListService watchListService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/signup")
    private ResponseEntity<User> register(@RequestBody SignUpRequest signUp){
        User emailExists=userRepository.findByEmail(signUp.getEmail());
        if(emailExists != null){
            throw new RuntimeException("Email is already taken");
        }
        User newUser=new User();
        newUser.setUserName(signUp.getUserName());
        newUser.setEmail(signUp.getEmail());
        newUser.setPassword(encoder.encode(signUp.getPassword()));
        User savedUser=userRepository.save(newUser);
        watchListService.createWatchList(savedUser);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }

    @PostMapping("/signin")
    private ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        Authentication authentication=authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        MyUserDetails myUserDetails=(MyUserDetails) authentication.getPrincipal();
        String jwtToken=jwtUtils.generateToken(myUserDetails);

        User user=userRepository.findByEmail(loginRequest.getEmail());

        if(user == null){
            throw new RuntimeException("user not found in the database");
        }
        if(user.getTwoFactorAuth().isEnabled()){
            LoginResponse response=new LoginResponse();
            response.setMessage("Two factor auth is enabled");
            response.setTwoFactorAuthEnabled(true);
            response.setStatus(true);
            String otp= OtpUtils.generateOtp();
            TwoFactorOtp oldTwoFactorOtp=twoFactorOtpService.findByUser(user.getId());
            if(oldTwoFactorOtp != null){
                twoFactorOtpService.deleteTwoFactorOtp(oldTwoFactorOtp);
            }
            TwoFactorOtp newTwoFactorOtp=twoFactorOtpService.createTwoFactorOtp(user,otp,jwtToken);
            emailService.sendVerificationOtp(user.getEmail(),otp);
            response.setSession(newTwoFactorOtp.getOtp());
            return new ResponseEntity<>(response,HttpStatus.ACCEPTED);
        }
        LoginResponse response=new LoginResponse();
        response.setJwtToken(jwtToken);
        response.setUserName(user.getUserName());
        response.setStatus(true);
        response.setMessage("Login successful");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/two-factor/otp/{otp}")
    public ResponseEntity<LoginResponse>verifySinginOtp(@PathVariable String otp, @RequestParam String id){
        TwoFactorOtp twoFactorOtp=twoFactorOtpService.findById(id);
        if(twoFactorOtpService.verifyTwoFactorOtp(twoFactorOtp,otp)){
            LoginResponse response=new LoginResponse();
            response.setMessage("Two factor authentication verified");
            response.setTwoFactorAuthEnabled(true);
            response.setJwtToken(twoFactorOtp.getJwt());
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else{
            throw new RuntimeException("Invalid otp");
        }
    }
}