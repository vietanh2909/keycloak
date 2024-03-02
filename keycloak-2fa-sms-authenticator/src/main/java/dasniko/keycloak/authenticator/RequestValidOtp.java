package dasniko.keycloak.authenticator;

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
