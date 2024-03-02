package dasniko.keycloak.authenticator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.FormAction;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class SmsAuthenticator implements Authenticator {

    private static final String MOBILE_NUMBER_FIELD = "mobile_number";
    private static final String TPL_CODE = "login-sms.ftl";

    private static final Logger LOGGER = Logger.getLogger(SmsAuthenticator.class.getName());

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        LOGGER.info("Start authentication custom....");
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        KeycloakSession session = context.getSession();
        UserModel user = context.getUser();

        String mobileNumber = user.getFirstAttribute(MOBILE_NUMBER_FIELD);
        // mobileNumber of course has to be further validated on proper format, country code, ...

        int length = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_LENGTH));
        int ttl = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_TTL));
        String requestId = SecretGenerator.getInstance().randomString(10, SecretGenerator.DIGITS);
        String transactionId = SecretGenerator.getInstance().randomString(12, SecretGenerator.DIGITS);


        RequestGenerateOtp req = RequestGenerateOtp.builder()
                .phone(mobileNumber)
                .transactionId(transactionId)
                .length(Integer.toString(length))
                .requestId(requestId)
                .build();

        String jsonObject = new Gson().toJson(req);

        //String jsonObject = convertObjetToString(req);

        String code = "111111";
        try {
            //String result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
            String result = postRestApi("http://172.19.0.5:8080/v1/otp/create", jsonObject);
            System.out.println("Result: " + result);
            ObjectMapper mapper = new ObjectMapper();

            ResponseGenerateOtp res = new Gson().fromJson(result, ResponseGenerateOtp.class);
            code = res.getOtp();
            transactionId = res.getTransactionId();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        //String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setAuthNote(SmsConstants.CODE, code);
        authSession.setAuthNote("transactionId", transactionId);
        authSession.setAuthNote(SmsConstants.CODE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
        LOGGER.info("End authentication custom....");

        try {
            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
            Locale locale = session.getContext().resolveLocale(user);
            String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
            String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));

            LOGGER.info("SMS Text: " + smsText);

            //SmsServiceFactory.get(config.getConfig()).send(mobileNumber, smsText);

            context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TPL_CODE));

        } catch (Exception e) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("smsAuthSmsNotSent", e.getMessage())
                            .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        LOGGER.info("Start action...");
        String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst(SmsConstants.CODE);

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        //LOGGER.info("AuthSession: " + new Gson().toJson(authSession));
        //context.get
        String code = authSession.getAuthNote(SmsConstants.CODE);
        LOGGER.info("Code is: " + code);
        String ttl = authSession.getAuthNote(SmsConstants.CODE_TTL);

        if (code == null || ttl == null) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            return;
        }

        RequestValidOtp req = new RequestValidOtp();
        req.setOtp(enteredCode);
        req.setTransactionId(authSession.getAuthNote("transactionId"));

        String jsonObject = new Gson().toJson(req);

        //ResponseValidOtp res = new ResponseValidOtp();
        boolean otpValid;
        try {

            String result = postRestApi("http://172.19.0.5:8080/v1/otp/verify", jsonObject);

            ResponseValidOtp res = new Gson().fromJson(result, ResponseValidOtp.class);
            LOGGER.info("Response Valid Message: " + result);
            LOGGER.info("Response Valid Message: " + new Gson().toJson(res));
            otpValid = res.isValid();

        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (otpValid) {
            LOGGER.info("Code is valid...");

            if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                LOGGER.info("Code is expired...");
                // expired
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                        context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
            } else {
                // valid
                LOGGER.info("Code is valid success...");
                context.success();
            }
        } else {
            // invalid
            LOGGER.info("Code is invalid...");
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isRequired()) {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                        context.form().setAttribute("realm", context.getRealm())
                                .setError("smsAuthCodeInvalid").createForm(TPL_CODE));
            } else if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
        }

    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.getFirstAttribute(MOBILE_NUMBER_FIELD) != null;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // this will only work if you have the required action from here configured:
        // https://github.com/dasniko/keycloak-extensions-demo/tree/main/requiredaction
        user.addRequiredAction("mobile-number-ra");
    }

    @Override
    public void close() {
    }

    private static String postRestApi(String uri, String objectJson) throws ExecutionException, InterruptedException, TimeoutException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectJson))
                .uri(URI.create(uri)).build();

        CompletableFuture<HttpResponse<String>> response =
                httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

        return response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

    }

}
