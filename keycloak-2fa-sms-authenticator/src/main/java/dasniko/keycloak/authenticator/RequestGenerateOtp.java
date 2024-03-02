package dasniko.keycloak.authenticator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestGenerateOtp {
	private String length;
	private String phone;
	private String requestId;
	private String transactionId;
}
