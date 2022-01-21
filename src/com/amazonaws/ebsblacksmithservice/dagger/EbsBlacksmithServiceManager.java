package com.amazonaws.ebsblacksmithservice.dagger;

import com.amazon.coral.bobcat.BobcatServer;
import com.amazon.coral.dagger.service.ServiceManager;
import com.amazon.coral.service.EnvironmentChecker;
import dagger.Binds;
import dagger.Lazy;
import dagger.Module;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EbsBlacksmithServiceManager implements ServiceManager {
    @Module
    static abstract class BindingModule {
        @Binds
        abstract ServiceManager bindServiceManager(EbsBlacksmithServiceManager manager);
    }

    private final Lazy<EnvironmentChecker> lazyEnvironmentChecker;
    private final BobcatServer bobcatServer;

    @Inject
    EbsBlacksmithServiceManager(
            Lazy<EnvironmentChecker> lazyEnvironmentChecker,
            BobcatServer bobcatServer
    ) {
        super();
        this.lazyEnvironmentChecker = lazyEnvironmentChecker;
        this.bobcatServer = bobcatServer;
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
    }

    @Override
    public void stop() throws Exception {
        bobcatServer.shutdown();
    }
}
