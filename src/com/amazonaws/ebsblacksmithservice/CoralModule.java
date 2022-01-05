package com.amazonaws.ebsblacksmithservice;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amazon.platform.tools.ApolloEnvironmentInfo;
import amazon.platform.tools.ApolloEnvironmentInfo.EnvironmentRootUndefinedException;

import com.amazon.coral.bobcat.Bobcat3EndpointConfig;
import com.amazon.coral.bobcat.BobcatServer;
import com.amazon.coral.guice.GuiceActivityHandler;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.coral.metrics.helper.QuerylogHelper;
import com.amazon.coral.service.ChainComponent;
import com.amazon.coral.service.EnvironmentChecker;
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
import com.amazon.guice.brazil.AppConfigBinder;
import com.amazonaws.ebsblacksmithservice.health.DeepHealthCheck;
import com.amazonaws.ebsblacksmithservice.health.ShallowHealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class CoralModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(CoralModule.class);

    private final String root;
    private final String realm;
    private final String domain;

    CoralModule(String root, String domain, String realm) {
        this.root = root;
        this.realm = realm;
        this.domain = domain;
    }

    @Override
    protected void configure() {
        AppConfigBinder appConfigBinder = new AppConfigBinder(binder());
        appConfigBinder.bindPrefix("*");

        try {
            bind(EnvironmentChecker.class)
                .toConstructor(EnvironmentChecker.class.getConstructor(MetricsFactory.class)).asEagerSingleton();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    MetricsFactory factory() {
        QuerylogHelper queryLogHelper = new QuerylogHelper();
        queryLogHelper.setFilename(root + "/var/output/logs/service_log");
        queryLogHelper.setProgram("EbsBlacksmithService");
        queryLogHelper.setMarketplace(String.format("EbsBlacksmithService:%s:%s", domain, realm));
        return queryLogHelper;
    }

    @Provides
    @Singleton
    BobcatServer getBobcatServer(Orchestrator coral, MetricsFactory metricsFactory) throws Throwable {
        Bobcat3EndpointConfig endpointConfig = new Bobcat3EndpointConfig();
        endpointConfig.setMetricsFactory(metricsFactory);
        endpointConfig.setOrchestrator(coral);
        endpointConfig.setNumThreads(32);
        endpointConfig.setEndpoints(Arrays.asList(
                Bobcat3EndpointConfig.uri(String.format("http://0.0.0.0:%d", getHttpRegularPortFromOpConfigOrDefault()))));
        return new BobcatServer(endpointConfig);
    }

    private static int getHttpRegularPortFromOpConfigOrDefault() {
        return getHttpPortFromOpConfigOrDefault("httpRegularPort", 8080);
    }

    private static int getHttpSecurePortFromOpConfigOrDefault() {
        return getHttpPortFromOpConfigOrDefault("httpSecurePort", 8443);
    }

    private static int getHttpPortFromOpConfigOrDefault(String portName, int defaultPort) {
        Optional<Integer> apolloEnvironmentInfoPort =
            getApolloEnvironmentInfo()
                .map(apolloEnvInfo -> apolloEnvInfo.getOperationalConfiguration("HttpServer"))
                .map(httpServerMap -> httpServerMap.get(portName))
                .map(portObject -> {
                    try {
                        return Integer.parseInt(portObject.toString());
                    } catch (NumberFormatException e) {
                        log.error("OpConfig value {} for HttpServer -> {} is not a number.", portObject, portName);
                        throw e;
                    }
                });

        return apolloEnvironmentInfoPort.orElseGet(() -> {
            // Fallback to property set by Coral "brazil-build server" target
            String portProperty = System.getProperty("apollo.OCF.HttpServer." + portName, Integer.toString(defaultPort));
            log.warn("OpConfig value HttpServer -> {} not found. Using {}.", portName, portProperty);
            try {
                return Integer.parseInt(portProperty);
            } catch (NumberFormatException e) {
                log.error("System property apollo.OCF.HttpServer.{} was {}"
                        + " and could not be parsed as an integer.", portName, portProperty);
                throw e;
            }
        });
    }

    private static Optional<ApolloEnvironmentInfo> getApolloEnvironmentInfo() {
        try {
            return Optional.of(new ApolloEnvironmentInfo());
        } catch (EnvironmentRootUndefinedException | IOException e) {
            log.warn("Could not get ApolloEnvironmentInfo.", e);
            return Optional.empty();
        }
    }

    @Provides
    @Singleton
    Orchestrator getCoral(MetricsFactory metricsFactory, Injector injector) throws Exception {

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

        handlerChain.add(new GuiceActivityHandler(injector).build());

        ChainHelper chainHelper = new ChainHelper();
        chainHelper.setHandlers(handlerChain);
        Orchestrator coral = new OrchestratorHelper(chainHelper, 30000);
        return coral;
    }
}
