package com.amazonaws.ebsblacksmithservice.dagger;

import amazon.platform.config.AppConfig;
import amazon.platform.config.AppConfigTree;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static com.amazonaws.ebsblacksmithservice.dagger.modules.EnvironmentModule.APP_NAME;

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
    @Named("blacksmith.placementDataFile")
    static String providePlacementDataFile(final AppConfigTree appConfigTree) {
        return appConfigTree.findString("blacksmith.placementDataFile");
    }
}
