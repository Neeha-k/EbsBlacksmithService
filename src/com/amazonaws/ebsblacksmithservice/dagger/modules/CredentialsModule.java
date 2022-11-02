package com.amazonaws.ebsblacksmithservice.dagger.modules;

import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.ebs.auth.TurtleAWSCredentialsProvider;
import com.amazonaws.ebsblacksmithservice.types.Domain;

@Module
public class CredentialsModule {
    private static final String TURTLE_ROLE_TEMPLATE = "%s-%s";
    @VisibleForTesting
    protected static final String ARPS_TURTLE_ROLE_NAME = "EbsBlacksmithServiceARPSRole-v0";

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

    @VisibleForTesting
    protected String getARPSRoleName(Domain domain, Boolean isIdm) {
        if(isIdm) {
            return String.format(TURTLE_ROLE_TEMPLATE, ARPS_TURTLE_ROLE_NAME, domain.toString().toLowerCase());
        }

        return String.format(TURTLE_ROLE_TEMPLATE, ARPS_TURTLE_ROLE_NAME, domain.toString().toLowerCase());
    }
}
