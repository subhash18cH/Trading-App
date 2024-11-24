package com.subhash.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String userName;
    private String email;
    private String password;
}

