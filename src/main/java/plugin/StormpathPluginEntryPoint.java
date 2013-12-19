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

package plugin;

import callbacks.Authentication;
import callbacks.Authorization;
import com.dcsquare.hivemq.spi.PluginEntryPoint;
import com.dcsquare.hivemq.spi.callback.registry.CallbackRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Lukas Brandl
 */

public class StormpathPluginEntryPoint extends PluginEntryPoint {

    protected final Authentication authentication;
    protected final Authorization authorization;

    @Inject
    public StormpathPluginEntryPoint(Authentication authentication, Authorization authorization) {
        this.authentication = authentication;
        this.authorization = authorization;
    }

    @PostConstruct
    public void postConstruct() {
        CallbackRegistry callbackRegistry = getCallbackRegistry();
        callbackRegistry.addCallback(authentication);
        callbackRegistry.addCallback(authorization);

    }
}
