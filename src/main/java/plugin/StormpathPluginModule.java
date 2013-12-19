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

import com.dcsquare.hivemq.spi.HiveMQPluginModule;
import com.dcsquare.hivemq.spi.PluginEntryPoint;
import com.dcsquare.hivemq.spi.callback.security.OnAuthenticationCallback;
import com.dcsquare.hivemq.spi.plugin.meta.Information;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.application.ApplicationList;
import com.stormpath.sdk.application.Applications;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.ClientBuilder;
import com.stormpath.sdk.client.DefaultApiKey;
import com.stormpath.sdk.tenant.Tenant;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static com.dcsquare.hivemq.spi.config.Configurations.newConfigurationProvider;
import static com.dcsquare.hivemq.spi.config.Configurations.newReloadablePropertiesConfiguration;

/**
 * @author Lukas Brandl
 */

@Information(name = "HiveMQ Stormpath Plugin", author = "Lukas Brandl", version = "1.0")
public class StormpathPluginModule extends HiveMQPluginModule {

    Logger log = LoggerFactory.getLogger(OnAuthenticationCallback.class);

    @Override
    public Provider<Iterable<? extends AbstractConfiguration>> getConfigurations() {
        return newConfigurationProvider(newReloadablePropertiesConfiguration("stormpathPlugin.properties", 5, TimeUnit.MINUTES));
    }

    @Override
    protected void configurePlugin() {
    }

    @Override
    protected Class<? extends PluginEntryPoint> entryPointClass() {
        return StormpathPluginEntryPoint.class;
    }

    @Provides
    @Singleton
    private Application registerApplication(final Configuration configuration) {
        try {
            final String apiKeyId = configuration.getString("stormpath.apiKey.id");
            final String apiKeySecret = configuration.getString("stormpath.apiKey.secret");

            final DefaultApiKey apiKey = new DefaultApiKey(apiKeyId, apiKeySecret);
            final Client client = new ClientBuilder().setApiKey(apiKey).build();
            final String applicationName = configuration.getString("stormpath.application.name");

            final Tenant tenant = client.getCurrentTenant();
            Application application = null;

            final ApplicationList applications = tenant.getApplications(Applications.where(Applications.name().eqIgnoreCase(applicationName)));
            final Iterator<Application> iterator = applications.iterator();

            if (iterator.hasNext()) {
                application = iterator.next();
            }

            if (application == null) {
                application = client.instantiate(Application.class);
                application.setName(applicationName);
                application = client.getCurrentTenant()
                        .createApplication(Applications.newCreateRequestFor(application).createDirectory().build());
            }

            return application;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }

}
