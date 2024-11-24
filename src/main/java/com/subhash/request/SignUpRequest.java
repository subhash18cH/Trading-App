package com.subhash.request;

import lombok.Data;

@Data
public class SignUpRequest {

    private String userName;
    private String email;
    private String password;
}
