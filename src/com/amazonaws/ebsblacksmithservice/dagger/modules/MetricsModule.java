package com.amazonaws.ebsblacksmithservice.dagger.modules;

import amazon.platform.config.AppConfigTree;
import amazon.platform.config.Realm;
import com.amazon.coral.dagger.config.ServiceEnvironment;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.coral.metrics.helper.QuerylogHelper;
import com.amazon.coral.metrics.helper.SensingMetricsHelper;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module(
        includes = {
                EnvironmentModule.class
        }
)
public class MetricsModule {

    @Provides
    @Singleton
    static MetricsFactory provideMetricsFactory(ServiceEnvironment environment, AppConfigTree appConfig,
                                                @Named("subZone") String subZone) {
        QuerylogHelper querylogHelper = new QuerylogHelper();
        querylogHelper.setFilename(environment.getEnvironmentRoot() + "/var/output/logs/service_log");

        SensingMetricsHelper metricsHelper = new SensingMetricsHelper();
        metricsHelper.setReporters(ImmutableList.of(querylogHelper));
        metricsHelper.setProgram(appConfig.getApplicationName());
        metricsHelper.setMarketplace(subZone);
        return metricsHelper;
    }
}
