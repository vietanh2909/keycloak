package org.example.rest.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestValidOtp {
    private String otp;
    private String transactionId;
}

