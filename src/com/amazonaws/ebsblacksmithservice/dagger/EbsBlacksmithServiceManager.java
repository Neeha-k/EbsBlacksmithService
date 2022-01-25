package com.amazonaws.ebsblacksmithservice.dagger;

import com.amazon.ebs.dfdd.DfddHeartbeatHelper;
import com.amazon.coral.bobcat.BobcatServer;
import com.amazon.coral.dagger.service.ServiceManager;
import com.amazon.coral.service.EnvironmentChecker;
import dagger.Binds;
import dagger.Lazy;
import dagger.Module;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.inject.Named;

@Singleton
public class EbsBlacksmithServiceManager implements ServiceManager {
    @Module
    static abstract class BindingModule {
        @Binds
        abstract ServiceManager bindServiceManager(EbsBlacksmithServiceManager manager);
    }

    private final Lazy<EnvironmentChecker> lazyEnvironmentChecker;
    private final BobcatServer bobcatServer;
    private final DfddHeartbeatHelper insecureDfddHeartbeatHelper;
    private final DfddHeartbeatHelper secureDfddHeartbeatHelper;

    @Inject
    EbsBlacksmithServiceManager(
            Lazy<EnvironmentChecker> lazyEnvironmentChecker,
            BobcatServer bobcatServer,
            @Named("DfddHeartbeatHelperRegular") DfddHeartbeatHelper insecureDfddHeartbeatHelper,
            @Named("DfddHeartbeatHelperSecure") DfddHeartbeatHelper secureDfddHeartbeatHelper
    ) {
        super();
        this.lazyEnvironmentChecker = lazyEnvironmentChecker;
        this.bobcatServer = bobcatServer;
        this.insecureDfddHeartbeatHelper = insecureDfddHeartbeatHelper;
        this.secureDfddHeartbeatHelper = secureDfddHeartbeatHelper;
    }

    @Override
    public void initialize(){
    }

    @Override
    public void verify() throws Exception {
        lazyEnvironmentChecker.get();
    }

    @Override
    public void start() throws Exception {
        bobcatServer.start();
        insecureDfddHeartbeatHelper.startHeartbeating();
        secureDfddHeartbeatHelper.startHeartbeating();
    }

    @Override
    public void stop() throws Exception {
        insecureDfddHeartbeatHelper.stopHeartbeating();
        secureDfddHeartbeatHelper.stopHeartbeating();
        bobcatServer.shutdown();
    }
}
