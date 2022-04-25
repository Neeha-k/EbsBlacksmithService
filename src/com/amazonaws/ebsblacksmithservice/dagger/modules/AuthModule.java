package com.amazonaws.ebsblacksmithservice.dagger.modules;

import aws.auth.client.error.ARCClientException;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.credentials.adapter.V1V2AwsCredentialProviderAdapter;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Map;

import com.amazon.coral.security.Authority;
import com.amazon.coral.service.helper.ChainHelper;
import com.amazon.ebs.auth.handlers.AuthChainHelperFactory;
import com.amazonaws.ebsblacksmithservice.auth.AuthorizationList;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import com.amazonaws.rip.models.region.IRegion;

@Module(
        includes = {
                CredentialsModule.class,
                EnvironmentModule.class
        }
)
public class AuthModule {
    private static final String VENDOR_CODE = "ebs-blacksmith-service";
    private static final String ARPS_SERVICE_NAME = VENDOR_CODE;

    @Provides
    @Singleton
    @Named("AuthChainHelper")
    static ChainHelper provideAuthChainHelper(@Named("ARPSCredentials") AwsCredentialsProvider arpsCredentialsProvider,
                                   Map<String, Authority> operationAuthorities,
                                   @Named("Region") IRegion region) {
        // Qualifier should always be prod.<region-name>
        // https://w.amazon.com/bin/view/AWSAuth/Integration#HWhichAuthenticationServicedoIuse3F
        String qualifier = "prod." + region.regionName();

        try {
            return AuthChainHelperFactory.buildAuthChain(
                    VENDOR_CODE,
                    ARPS_SERVICE_NAME,
                    qualifier,
                    region.regionName(),
                    V1V2AwsCredentialProviderAdapter.adapt(arpsCredentialsProvider),
                    operationAuthorities
            );
        } catch (ARCClientException e) {
            throw new RuntimeException("Problem encountered when constructing the AuthChainHelper", e);
        }
    }

    @Provides
    @Singleton
    static Map<String, Authority> provideOperationAuthorities(
            @Named("Domain") Domain domain,
            @Named("subZone") String zone,
            @Named("Region") IRegion region) {
        return AuthorizationList.getApiToAuthority(domain, zone, region);
    }
}
