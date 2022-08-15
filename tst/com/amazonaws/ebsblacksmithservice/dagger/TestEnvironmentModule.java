package com.amazonaws.ebsblacksmithservice.dagger;

import static com.amazonaws.ebsblacksmithservice.dagger.modules.EnvironmentModule.APP_NAME;

import amazon.platform.config.AppConfig;
import amazon.platform.config.AppConfigTree;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.coral.metrics.NullMetricsFactory;
import dagger.Module;
import dagger.Provides;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class TestEnvironmentModule {

    @Provides
    @Singleton
    @Named("Root")
    static String provideRoot() {
        return System.getProperty("root");
    }

    @Provides
    @Singleton
    static AppConfigTree provideAppConfigTree() {

        List<String> args = new ArrayList<>();
        args.add("--domain=test");
        args.add("--realm=us-west-2");
        args.add("--root=./configuration/");
        AppConfig.initialize(
                APP_NAME,
                APP_NAME,
                args.toArray(new String[args.size()]));

        return AppConfig.instance();
    }

    @Provides
    @Singleton
    @Named("blacksmith.serverPlacementDataFile")
    static String provideServerPlacementDataFile(final AppConfigTree appConfigTree) {
        return appConfigTree.findString("blacksmith.serverPlacementDataFile");
    }

    @Provides
    @Singleton
    @Named("blacksmith.diskPlacementDataFile")
    static String provideDiskPlacementDataFile(final AppConfigTree appConfigTree) {
        return appConfigTree.findString("blacksmith.diskPlacementDataFile");
    }

    @Provides
    @Singleton
    static MetricsFactory provideMetricsFactory() {
        return new NullMetricsFactory();
    }
}


