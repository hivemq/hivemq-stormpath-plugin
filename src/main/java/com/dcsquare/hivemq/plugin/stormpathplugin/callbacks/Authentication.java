/*
 * Copyright 2013 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dcsquare.hivemq.plugin.stormpathplugin.callbacks;

import com.dcsquare.hivemq.spi.callback.CallbackPriority;
import com.dcsquare.hivemq.spi.callback.exception.AuthenticationException;
import com.dcsquare.hivemq.spi.callback.security.OnAuthenticationCallback;
import com.dcsquare.hivemq.spi.message.ReturnCode;
import com.dcsquare.hivemq.spi.security.ClientCredentialsData;
import com.google.common.annotations.VisibleForTesting;
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

    protected final Logger log = LoggerFactory.getLogger(OnAuthenticationCallback.class);
    protected final Application application;
    protected Account account;

    @Inject
    public Authentication(Application application) {
        this.application = application;
    }

    @Override
    //@Cached(timeToLive = 5, timeUnit = TimeUnit.MINUTES)
    public Boolean checkCredentials(ClientCredentialsData clientData) throws AuthenticationException {


        if (!clientData.getUsername().isPresent()) {
            log.info("Authentication failed " + clientData.getClientId());
            throw new AuthenticationException("No Username provided", ReturnCode.REFUSED_NOT_AUTHORIZED);
        }
        String username = clientData.getUsername().get();

        if (Strings.isNullOrEmpty(username)) {
            log.info("Authentication failed " + clientData.getClientId());
            throw new AuthenticationException("No Username provided", ReturnCode.REFUSED_NOT_AUTHORIZED);
        }

        account = getAuthenticatedAccount(username, clientData.getPassword().or(""));
        if (account != null) {
            log.info("Authentication successful " + clientData.getClientId());
            return true;
        }
        log.info("Authentication failed " + clientData.getClientId());
        return false;
    }

    @Override
    public int priority() {
        return CallbackPriority.MEDIUM;
    }

    @VisibleForTesting
    Account getAuthenticatedAccount(final String username, final String password) {
        final AuthenticationRequest request = new UsernamePasswordRequest(username, password);

        try {
            return application.authenticateAccount(request).getAccount();
        } catch (ResourceException e) {
            log.error("Auth error: " + e.getDeveloperMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            request.clear();
        }
        return null;
    }


}
