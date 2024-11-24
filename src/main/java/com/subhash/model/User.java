package com.subhash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.subhash.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;

    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Embedded
    private TwoFactorAuth twoFactorAuth =new TwoFactorAuth();

    private Role role=Role.ROLE_CUSTOMER;
}
