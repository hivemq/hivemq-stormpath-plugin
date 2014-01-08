package com.dcsquare.hivemq.plugin.stormpathplugin.callbacks;

import com.dcsquare.hivemq.spi.callback.exception.AuthenticationException;
import com.dcsquare.hivemq.spi.security.ClientCredentialsData;
import com.google.common.base.Optional;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Lukas Brandl
 */
public class AuthenticationTest {
    @Mock
    Application app;

    @Mock
    ClientCredentialsData clientData;

    @Mock
    Account account;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = AuthenticationException.class)
    public void test_exception_on_absent_username() throws Exception {

        when(clientData.getUsername()).thenReturn(Optional.<String>absent());
        Authentication authentication = new Authentication(app);
        authentication.checkCredentials(clientData);
    }

    @Test(expected = AuthenticationException.class)
    public void test_exception_on_empty_username() throws Exception {
        when(clientData.getUsername()).thenReturn(Optional.of(""));
        Authentication authentication = new Authentication(app);
        authentication.checkCredentials(clientData);
    }

    @Test
    public void test_authentication_successful() throws Exception {
        when(clientData.getUsername()).thenReturn(Optional.of("test"));
        when(clientData.getPassword()).thenReturn(Optional.of("pw"));
        TestAuthentication testAuthentication = new TestAuthentication(app, account);
        assertTrue(testAuthentication.checkCredentials(clientData));
    }

    @Test
    public void test_authentication_failed() throws Exception {
        when(clientData.getUsername()).thenReturn(Optional.of("test"));
        when(clientData.getPassword()).thenReturn(Optional.of("pw"));
        TestAuthentication testAuthentication = new TestAuthentication(app, null);
        assertFalse(testAuthentication.checkCredentials(clientData));
    }

    class TestAuthentication extends Authentication {

        private final Account account;

        TestAuthentication(Application application, Account account) {
            super(application);
            this.account = account;
        }

        @Override
        Account getAuthenticatedAccount(final String username, final String password) {
            return account;
        }
    }
}
