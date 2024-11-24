package com.subhash.service;

import com.subhash.enums.VerificationType;
import com.subhash.model.TwoFactorAuth;
import com.subhash.model.User;
import com.subhash.repository.UserRepository;
import com.subhash.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    public User findUserProfileByJwt(String jwt){
        String email= jwtUtils.getEmailFromJwtToken(jwt);
        User user=userRepository.findByEmail(email);
        if(user == null){
            throw new RuntimeException("User not found with this name and email");
        }
        return user;
    }

    public User findUserByEmail(String email){
        User user=userRepository.findByEmail(email);
        if(user == null){
            throw new RuntimeException("User not found with given email");
        }
        return user;
    }

    public User findUserById(Long userId){
        Optional<User> user=userRepository.findById(userId);
        if(user.isEmpty()){
            throw new RuntimeException("User not found with given user id");
        }
        return user.get();
    }

    public User enableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user){
        TwoFactorAuth twoFactorAuth=new TwoFactorAuth();
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setSendTo(verificationType);
        user.setTwoFactorAuth(twoFactorAuth);
        return  userRepository.save(user);
    }

    public User updatePassword(User user,String newPassword){
        user.setPassword(encoder.encode(newPassword));
        return  userRepository.save(user);
    }
}