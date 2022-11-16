package com.amazonaws.ebsblacksmithservice.dagger.modules;

import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.turtle.monitoring.TurtleCredentialsMonitor;
import com.amazonaws.ebs.auth.TurtleAWSCredentialsProvider;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import com.amazonaws.rip.models.region.IRegion;
import com.google.common.annotations.VisibleForTesting;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Paths;

@Module
public class CredentialsModule {
    private static final String TURTLE_ROLE_TEMPLATE = "%s-%s";
    @VisibleForTesting
    protected static final String ARPS_TURTLE_ROLE_NAME = "EbsBlacksmithServiceARPSRole-v0";
    // https://tiny.amazon.com/oljrgcvx/codeamazpackEbsTblob2d6econf
    private static final String TURTLE_CONFIG_RELATIVE_PATH = "/var/turtle-config";
    @Provides
    @Singleton
    @Named("ARPSCredentials")
    public AwsCredentialsProvider getTurtleARPSCredentials(@Named("Domain") final Domain domain,
        @Named("IsIdm") Boolean isIdm) {
            return TurtleAWSCredentialsProvider.builder()
                .serviceName(EnvironmentModule.APP_NAME)
                .domain(domain.toString().toLowerCase())
                .roleName(getARPSRoleName(domain, isIdm))
                .build();
    }

    @Provides
    @Singleton
    @Named("TurtleCredentialsMonitor")
    public TurtleCredentialsMonitor getTurtleCredentialsMonitor(@Named("Region") IRegion region,
                                                                @Named("Domain") final Domain domain,
                                                                final MetricsFactory metricsFactory,
                                                                @Named("Root") String root) {
        final TurtleCredentialsMonitor turtleCredentialsMonitor = new TurtleCredentialsMonitor();
        turtleCredentialsMonitor.setDomain(domain.toString().toLowerCase());
        turtleCredentialsMonitor.setMetricsFactory(metricsFactory);
        turtleCredentialsMonitor.setRealm(region.regionName());
        turtleCredentialsMonitor.setConfigPath(Paths.get(root, TURTLE_CONFIG_RELATIVE_PATH).toString());
        turtleCredentialsMonitor.setRoleDimensionEnabled(true);
        return turtleCredentialsMonitor;
    }

    @VisibleForTesting
    protected String getARPSRoleName(Domain domain, Boolean isIdm) {
        if(isIdm) {
            return String.format(TURTLE_ROLE_TEMPLATE, ARPS_TURTLE_ROLE_NAME, domain.toString().toLowerCase());
        }

        return String.format(TURTLE_ROLE_TEMPLATE, ARPS_TURTLE_ROLE_NAME, domain.toString().toLowerCase());
    }
}
