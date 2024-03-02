package dasniko.keycloak.authenticator;

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
