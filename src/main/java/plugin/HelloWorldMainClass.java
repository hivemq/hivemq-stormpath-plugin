package plugin;

import callbacks.*;
import com.dcsquare.hivemq.spi.PluginEntryPoint;
import com.dcsquare.hivemq.spi.callback.registry.CallbackRegistry;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.application.Applications;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.ClientBuilder;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * This is the main class of the plugin, which is instanciated during the HiveMQ start up process.
 */
public class HelloWorldMainClass extends PluginEntryPoint {
    private Client client;
//    private Application application;

    Logger log = LoggerFactory.getLogger(HelloWorldMainClass.class);

    private final Configuration configuration;
    private Authentication authentication;

    /**
     * @param configuration Injected configuration, which is declared in the {@link HelloWorldPluginModule}.
     */
    @Inject
    public HelloWorldMainClass(Configuration configuration, Authentication authentication) {
        this.configuration = configuration;
        this.authentication = authentication;
    }

    /**
     * This method is executed after the instanciation of the whole class. It is used to initialize
     * the implemented callbacks and make them known to the HiveMQ core.
     */
    @PostConstruct
    public void postConstruct() {
        CallbackRegistry callbackRegistry = getCallbackRegistry();
        callbackRegistry.addCallback(authentication);
    }
}
