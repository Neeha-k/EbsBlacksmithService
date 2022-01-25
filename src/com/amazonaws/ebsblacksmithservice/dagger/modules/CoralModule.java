package com.amazonaws.ebsblacksmithservice.dagger.modules;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.amazon.coral.service.ActivityHandler;
import com.amazon.coral.service.RequestLoggingInterceptor;
import com.amazon.coral.validate.ValidationInterceptor;
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

import javax.inject.Named;
import javax.inject.Singleton;

@Module(
        includes = {
                EnvironmentModule.class
        }
)
public class CoralModule {

    private static final Logger log = LoggerFactory.getLogger(CoralModule.class);

    @Provides
    @Singleton
    static BobcatServer provideBobcatServer(Orchestrator coral, MetricsFactory metricsFactory,
            @Named("httpPort") int httpRegularPort) {
        Bobcat3EndpointConfig endpointConfig = new Bobcat3EndpointConfig();
        endpointConfig.setMetricsFactory(metricsFactory);
        endpointConfig.setOrchestrator(coral);
        endpointConfig.setNumThreads(32);
        endpointConfig.setEndpoints(Arrays.asList(
                Bobcat3EndpointConfig.uri(String.format("http://0.0.0.0:%d", httpRegularPort))));
        return new BobcatServer(endpointConfig);
    }

    @Provides
    @Singleton
    static Orchestrator provideOrchestrator(ActivityHandler activityHandler) {

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

        handlerChain.add(activityHandler);

        ChainHelper chainHelper = new ChainHelper();
        chainHelper.setHandlers(handlerChain);
        Orchestrator coral = new OrchestratorHelper(chainHelper, 30000);
        return coral;
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
