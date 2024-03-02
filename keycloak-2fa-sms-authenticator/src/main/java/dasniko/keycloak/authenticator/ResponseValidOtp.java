package dasniko.keycloak.authenticator;

import lombok.*;


@NoArgsConstructor
@Data
public class ResponseValidOtp {
	private String message;
	private boolean valid;
}
