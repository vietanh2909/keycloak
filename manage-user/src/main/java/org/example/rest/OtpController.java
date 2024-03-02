package org.example.rest;

import org.example.rest.request.RequestGenerateOtp;
import org.example.rest.request.RequestValidOtp;
import org.example.rest.response.ResponseGenerateOtp;
import org.example.rest.response.ResponseMessage;
import org.example.rest.response.ResponseValidOtp;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/otp", produces = MediaType.APPLICATION_JSON_VALUE)
public class OtpController {

    @PostMapping("/create")
    public ResponseGenerateOtp generateOtp(@RequestBody RequestGenerateOtp requestGenerateOtp) {

        return ResponseGenerateOtp.builder()
                .otp("123456")
                .message("Success")
                .requestId(requestGenerateOtp.getRequestId())
                .transactionId(requestGenerateOtp.getTransactionId())
                .build();
    }

    @PostMapping("/verify")
    public ResponseValidOtp verifyOtp(@RequestBody RequestValidOtp requestValidOtp) {

        if(requestValidOtp.getOtp().equals("123456"))
            return ResponseValidOtp.builder()
                    .message("Success")
                    .valid(true)
                    .build();
        return ResponseValidOtp.builder()
                .message("Invalid OTP")
                .valid(false)
                .build();

    }
}
