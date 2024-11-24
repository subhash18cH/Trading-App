package com.subhash.repository;

import com.subhash.model.ForgotPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPasswordToken,String > {
    ForgotPasswordToken findByUserId(Long userId);
}

