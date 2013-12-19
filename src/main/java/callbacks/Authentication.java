package callbacks;

import com.dcsquare.hivemq.spi.callback.CallbackPriority;
import com.dcsquare.hivemq.spi.callback.exception.AuthenticationException;
import com.dcsquare.hivemq.spi.callback.security.OnAuthenticationCallback;
import com.dcsquare.hivemq.spi.message.ReturnCode;
import com.dcsquare.hivemq.spi.security.ClientCredentialsData;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.authc.UsernamePasswordRequest;
import com.stormpath.sdk.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lukas Brandl
 */

public class Authentication implements OnAuthenticationCallback {

    Logger log = LoggerFactory.getLogger(OnAuthenticationCallback.class);
    Application application;

    @Inject
    public Authentication(Application application) {
        this.application = application;
    }

    @Override
    public Boolean checkCredentials(ClientCredentialsData clientData) throws AuthenticationException {

        String username;
        String password;
        if (!clientData.getUsername().isPresent()) {
            log.info("Authentication failed " + clientData.getClientId());
            throw new AuthenticationException("No Username provided", ReturnCode.REFUSED_NOT_AUTHORIZED);
        }
        username = clientData.getUsername().get();

        if (Strings.isNullOrEmpty(username)) {
            log.info("Authentication failed " + clientData.getClientId());
            throw new AuthenticationException("No Username provided", ReturnCode.REFUSED_NOT_AUTHORIZED);
        }

        if (!clientData.getPassword().isPresent()) {
            password = "";
        } else {
            password = clientData.getPassword().get();
            if (password == null) {
                password = "";
            }
        }

        Account account = getAuthenticatedAccount(username, password);
        if (account != null) {
            log.info("Authentication successful " + clientData.getClientId());
            return true;
        }
        log.info("Authentication failed " + clientData.getClientId());
        throw new AuthenticationException("Invalid username or password", ReturnCode.REFUSED_NOT_AUTHORIZED);
    }

    @Override
    public int priority() {
        return CallbackPriority.MEDIUM;
    }

    private Account getAuthenticatedAccount(String username, String password) {
        AuthenticationRequest request = new UsernamePasswordRequest(username, password);

        try {
            return application.authenticateAccount(request).getAccount();
        } catch (ResourceException e) {
            log.error("Auth error: " + e.getDeveloperMessage());
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            request.clear();
        }
    }


}