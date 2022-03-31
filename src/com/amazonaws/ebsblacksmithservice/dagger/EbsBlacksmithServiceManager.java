package com.amazonaws.ebsblacksmithservice.dagger;

import dagger.Binds;
import dagger.Lazy;
import dagger.Module;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.amazon.coral.bobcat.BobcatServer;
import com.amazon.coral.dagger.service.ServiceManager;
import com.amazon.coral.service.EnvironmentChecker;
import com.amazon.ebs.dfdd.DfddHeartbeatHelper;
import com.google.common.collect.ImmutableList;

@Singleton
public class EbsBlacksmithServiceManager implements ServiceManager {
    @Module
    static abstract class BindingModule {
        @Binds
        abstract ServiceManager bindServiceManager(EbsBlacksmithServiceManager manager);
    }

    private final Lazy<EnvironmentChecker> lazyEnvironmentChecker;
    private final DfddHeartbeatHelper insecureDfddHeartbeatHelper;
    private final DfddHeartbeatHelper secureDfddHeartbeatHelper;
    private final EndpointManager endpointManager;

    @Inject
    EbsBlacksmithServiceManager(
            Lazy<EnvironmentChecker> lazyEnvironmentChecker,
            @Named("InsecureBobcat") BobcatServer insecureBobcatServer,
            @Named("SecureBobcat") BobcatServer secureBobcatServer,
            @Named("DfddHeartbeatHelperRegular") DfddHeartbeatHelper insecureDfddHeartbeatHelper,
            @Named("DfddHeartbeatHelperSecure") DfddHeartbeatHelper secureDfddHeartbeatHelper
    ) {
        super();
        this.lazyEnvironmentChecker = lazyEnvironmentChecker;
        this.insecureDfddHeartbeatHelper = insecureDfddHeartbeatHelper;
        this.secureDfddHeartbeatHelper = secureDfddHeartbeatHelper;

        this.endpointManager = new EndpointManager(ImmutableList.of(secureBobcatServer, insecureBobcatServer));
    }

    @Override
    public void initialize() {
    }

    @Override
    public void verify() {
        lazyEnvironmentChecker.get();
    }

    @Override
    public void start() throws Exception {
        endpointManager.start();
        insecureDfddHeartbeatHelper.startHeartbeating();
        secureDfddHeartbeatHelper.startHeartbeating();
    }

    @Override
    public void stop() throws Exception {
        insecureDfddHeartbeatHelper.stopHeartbeating();
        secureDfddHeartbeatHelper.stopHeartbeating();
        endpointManager.shutdown();
    }
}
