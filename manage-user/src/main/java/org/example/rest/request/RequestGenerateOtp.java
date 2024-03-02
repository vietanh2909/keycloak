package org.example.rest.request;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestGenerateOtp {
    private String length;
    private String phone;
    private String requestId;
    private String transactionId;
}
