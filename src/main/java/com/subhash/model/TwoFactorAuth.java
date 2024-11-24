package com.subhash.model;

import com.subhash.enums.VerificationType;
import lombok.Data;

@Data
public class TwoFactorAuth {

    private boolean isEnabled=false;
    private VerificationType sendTo;
}