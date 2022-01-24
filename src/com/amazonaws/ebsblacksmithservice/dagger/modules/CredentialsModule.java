package com.amazonaws.ebsblacksmithservice.dagger.modules;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module()
public class CredentialsModule {

    /**
     * This should be replaced with Turtle.
     * Tracking in: https://sim.amazon.com/issues/Glycerin-306
     */
    @Provides
    @Singleton
    static AWSCredentialsProvider provideDefaultCredentialProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }
}
