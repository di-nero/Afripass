package com.AfriPass.afripass.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentStatus {

    SUCCEEDED("succeeded");

    private final String value;

}
