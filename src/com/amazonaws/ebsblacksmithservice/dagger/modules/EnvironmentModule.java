package com.amazonaws.ebsblacksmithservice.dagger.modules;

import amazon.platform.config.AppConfig;
import amazon.platform.config.AppConfigTree;
import amazon.platform.config.Realm;
import amazon.platform.tools.ApolloEnvironmentInfo;
import com.amazon.coral.dagger.config.ApolloServiceEnvironment;
import com.amazon.coral.dagger.config.ServiceEnvironment;
import com.amazon.coral.dagger.config.WorkspaceServiceEnvironment;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.coral.service.EnvironmentChecker;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Module
public class EnvironmentModule {

    private static final String APP_NAME = "EbsBlacksmithService";

    @Provides
    @Singleton
    static EnvironmentChecker provideEnvironmentChecker(MetricsFactory metricsFactory) {
        return new EnvironmentChecker(metricsFactory);
    }

    @Provides
    @Singleton
    @Named("Domain")
    static Domain provideDomain() {
        return Domain.fromString(AppConfig.getDomain());
    }

    @Provides
    @Singleton
    @Named("Realm")
    static Realm provideRealm() {
        return AppConfig.getRealm();
    }

    @Provides
    @Singleton
    @Named("Root")
    static String provideRoot() {
        return System.getProperty("root");
    }

    /*
     * Copied from EnvironmentModule in AmazonCoralDaggerSupportRuntime
     * The one provided by AmazonCoralDaggerSupportRuntime requires a initialized AppConfig instance
     * And that's not possible with Dagger as main() method is in Dagger generated file
     * We have to initialize AppConfig by ourself
     */
    @Provides
    @Singleton
    static ServiceEnvironment provideServiceEnvironment() {
        try {
            return new ApolloServiceEnvironment(new ApolloEnvironmentInfo());
        } catch (ApolloEnvironmentInfo.EnvironmentRootUndefinedException e) {
            System.out.println("**************************************************************************************");
            System.out.println("** WARNING: Unable to initialize from Apollo, attempting to use workspace overrides **");
            System.out.println("** IF THIS IS HAPPENING IN AN APOLLO ENVIRONMENT, THEN SOMETHING IS SERIOUSLY WRONG **");
            System.out.println("**************************************************************************************");
            return new WorkspaceServiceEnvironment(System.getProperties());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Copied from EnvironmentModule in AmazonCoralDaggerSupportRuntime
     * This is initialize AppConfig for us
     */
    @Provides
    @Singleton
    static AppConfigTree provideAppConfigTree(ServiceEnvironment env) {
        String domain = env.getString("PubSub", "domain");
        String realm = env.getString("PubSub", "realm");
        String root = env.getEnvironmentRoot();

        List<String> args = new ArrayList<>();
        args.add("--domain=" + domain);
        args.add("--realm=" + realm);
        args.add("--root=" + root);

        AppConfig.initialize(
                APP_NAME,
                APP_NAME,
                args.toArray(new String[args.size()]));

        return AppConfig.instance();
    }
}
