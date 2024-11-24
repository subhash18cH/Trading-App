package com.subhash.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String otp;
    public String password;

}
