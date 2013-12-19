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
import com.stormpath.sdk.tenant.Tenant;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.dcsquare.hivemq.spi.config.Configurations.newConfigurationProvider;
import static com.dcsquare.hivemq.spi.config.Configurations.newReloadablePropertiesConfiguration;


/**
 * This is the plugin module class, which handles the initialization and configuration
 * of the plugin. Each plugin need to have a class, which is extending {@link HiveMQPluginModule}.
 * Also the fully qualified name of the class should be present in a file named
 * com.dcsquare.hivemq.spi.HiveMQPluginModule, which has to be located in META-INF/services.
 *
 * @author Christian Goetz
 */
@Information(name = "HiveMQ HelloWorld Plugin", author = "Christian Goetz", version = "1.0")
public class HelloWorldPluginModule extends HiveMQPluginModule {

    Logger log = LoggerFactory.getLogger(OnAuthenticationCallback.class);

    /**
     * This method can be used to add own configuration items for the plugin. The method accepts an
     * AbstractConfiguration from Apache Commons as return value, which gives great flexibility in
     * adding custom configurations. Some helper methods for basic configurations can be found in
     * {@link com.dcsquare.hivemq.spi.config.Configurations}. For acme
     * {@link com.dcsquare.hivemq.spi.config.Configurations.noConfigurationNeeded()} returns an
     * empty configuration, if the plugin does not need one.
     * <p/>
     * The configuration file need to be located in the plugin folder!
     *
     * @return Any AbstractConfiguration from Apache Commons, or the return value of the helper methods
     *         in {@link com.dcsquare.hivemq.spi.config.Configurations}
     */
    @Override
    public Provider<Iterable<? extends AbstractConfiguration>> getConfigurations() {
        return newConfigurationProvider(newReloadablePropertiesConfiguration("myPlugin.properties", 5, TimeUnit.MINUTES));
    }

    /**
     * This method is provided to execute some custom plugin configuration stuff. Is is the place
     * to execute Google Guice bindings,etc if needed.
     */
    @Override
    protected void configurePlugin() {
    }

    /**
     * This method needs to return the main class of the plugin.
     *
     * @return callback priority
     */
    @Override
    protected Class<? extends PluginEntryPoint> entryPointClass() {
        return HelloWorldMainClass.class;
    }

    @Provides
    @Singleton
    private Application registerApplication(Configuration configuration) {
        try {
            final String path = configuration.getString("apiKeyPath") + "/apiKey.properties";
            final Client client = new ClientBuilder().setApiKeyFileLocation(path).build();
            final String applicationName = configuration.getString("applicationName");

            Tenant tenant = client.getCurrentTenant();
            Application currentApplication = null;

            ApplicationList applications = tenant.getApplications();
            for (Application application : applications) {
                if (application.getName().equals(applicationName)) {
                    currentApplication = application;
                }
            }

            if (currentApplication == null) {
                currentApplication = client.instantiate(Application.class);
                currentApplication.setName(applicationName);
                currentApplication = client.getCurrentTenant()
                        .createApplication(Applications.newCreateRequestFor(currentApplication).createDirectory().build());
            }

            return currentApplication;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }

}
