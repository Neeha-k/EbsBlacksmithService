package com.amazonaws.ebsblacksmithservice.dagger.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.amazon.coral.bobcat.FileKeystoreConfig;
import com.amazon.coral.bobcat.SelfSignedKeystoreConfig;
import com.amazon.coral.service.ActivityHandler;
import com.amazon.coral.service.RequestLoggingInterceptor;
import com.amazon.coral.validate.ValidationInterceptor;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import dagger.Module;
import dagger.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amazon.platform.tools.ApolloEnvironmentInfo;
import amazon.platform.tools.ApolloEnvironmentInfo.EnvironmentRootUndefinedException;

import com.amazon.coral.bobcat.Bobcat3EndpointConfig;
import com.amazon.coral.bobcat.BobcatServer;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.coral.service.ChainComponent;
import com.amazon.coral.service.HttpAwsJson11Handler;
import com.amazon.coral.service.HttpHandler;
import com.amazon.coral.service.HttpRpcHandler;
import com.amazon.coral.service.Log4jAwareRequestIdHandler;
import com.amazon.coral.service.ModelHandler;
import com.amazon.coral.service.Orchestrator;
import com.amazon.coral.service.PingHandler;
import com.amazon.coral.service.ServiceHandler;
import com.amazon.coral.service.helper.ChainHelper;
import com.amazon.coral.service.helper.OrchestratorHelper;
import com.amazon.coral.service.http.ContentHandler;
import com.amazonaws.ebsblacksmithservice.health.DeepHealthCheck;
import com.amazonaws.ebsblacksmithservice.health.ShallowHealthCheck;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

@Module(
        includes = {
                AuthModule.class,
                EnvironmentModule.class
        }
)
public class CoralModule {

    private static final String AWS_DEFAULT_TLS_POLICY = "aws.default";
    private static final String LOOPBACK_DEVICE = "127.0.0.1";
    private static final String DEFAULT_ADDRESS = "0.0.0.0";

    private static final Logger log = LoggerFactory.getLogger(CoralModule.class);

    @Provides
    @Singleton
    @Named("InsecureBobcat")
    static BobcatServer provideBobcatServer(ActivityHandler activityHandler,
                                            MetricsFactory metricsFactory,
                                            @Named("Domain") Domain domain,
                                            @Named("httpPort") int httpRegularPort) {
        var coralOrchestrator = getCoralOrchestrator(activityHandler, null);
        var endpointConfig = getDefaultBobcat3EndpointConfig(coralOrchestrator, metricsFactory);
        String address;
        if (Domain.isSecureDomain(domain)) {
            // For secure domains, we don't want to expose an insecure endpoint to the outside world,
            // so we map it to the loopback device
            address = LOOPBACK_DEVICE;
        } else {
            address = DEFAULT_ADDRESS;
        }

        endpointConfig.setEndpoints(List.of(
                Bobcat3EndpointConfig.uri(String.format("http://%s:%d", address, httpRegularPort))
        ));

        return new BobcatServer(endpointConfig);
    }

    @Provides
    @Singleton
    @Named("SecureBobcat")
    static BobcatServer provideBobcatServerSecure(ActivityHandler activityHandler,
                                                  @Named("AuthChainHelper") ChainHelper authChain,
                                                  MetricsFactory metricsFactory, @Named("Domain") Domain domain,
                                                  @Named("Root") String root, @Named("httpSecurePort") int securePort) {
        var coralOrchestrator = getCoralOrchestrator(activityHandler, authChain);
        var endpointConfig = getDefaultBobcat3EndpointConfig(coralOrchestrator, metricsFactory);

        endpointConfig.setEndpoints(List.of(
                Bobcat3EndpointConfig.uri(String.format("https://0.0.0.0:%d", securePort))
        ));
        endpointConfig.setTlsPolicy(AWS_DEFAULT_TLS_POLICY);

        if (Domain.isSecureDomain(domain)) {
            endpointConfig.setKeystoreConfig(new FileKeystoreConfig(root + "/var/hostCert.jks", "amazon", "JKS", true));
        } else {
            // In unsecure domains, such as your local machine, we don't have certificates available, so we use
            // self-signed certificates.
            endpointConfig.setKeystoreConfig(new SelfSignedKeystoreConfig());
        }

        return new BobcatServer(endpointConfig);
    }

    static Bobcat3EndpointConfig getDefaultBobcat3EndpointConfig(Orchestrator coral, MetricsFactory metricsFactory) {
        return new Bobcat3EndpointConfig()
                .setMetricsFactory(metricsFactory)
                .setOrchestrator(coral)
                .setNumThreads(32);
    }

    private static Optional<ApolloEnvironmentInfo> getApolloEnvironmentInfo() {
        try {
            return Optional.of(new ApolloEnvironmentInfo());
        } catch (EnvironmentRootUndefinedException | IOException e) {
            log.warn("Could not get ApolloEnvironmentInfo.", e);
            return Optional.empty();
        }
    }

    private static Orchestrator getCoralOrchestrator(ActivityHandler activityHandler, @Nullable ChainHelper authChain) {
        List<ChainComponent> handlerChain = new ArrayList<>();
        handlerChain.add(new Log4jAwareRequestIdHandler());
        handlerChain.add(new HttpHandler());
        handlerChain.add(new PingHandler(new ShallowHealthCheck()));

        PingHandler deepPingHandler = new PingHandler(new DeepHealthCheck());
        deepPingHandler.setURIs(Collections.singletonList("/deep_ping"));
        handlerChain.add(deepPingHandler);

        ContentHandler contentHandler = new ContentHandler();
        handlerChain.add(contentHandler);

        handlerChain.add(new ServiceHandler("EbsBlacksmithService"));
        handlerChain.add(new HttpAwsJson11Handler());
        handlerChain.add(new HttpRpcHandler());
        handlerChain.add(new ModelHandler());

        // Authentication chain needs to come after wire protocols and before Activity handler
        // https://w.amazon.com/index.php/Coral/Manual/HttpSigning/AWS#Configuring_your_service_chain
        if (authChain != null) {
            handlerChain.add(authChain);
        }

        handlerChain.add(activityHandler);
        ChainHelper chainHelper = new ChainHelper();
        chainHelper.setHandlers(handlerChain);
        return new OrchestratorHelper(chainHelper, 30000);
    }

    @Provides
    @Singleton
    static ValidationInterceptor provideValidationInterceptor() {
        return new ValidationInterceptor();
    }

    @Provides
    @Singleton
    static RequestLoggingInterceptor provideRequestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }
}
