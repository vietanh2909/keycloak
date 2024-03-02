package org.example.rest.response;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseGenerateOtp {
    private String otp;
    private String message;
    private String requestId;
    private String transactionId;
}
