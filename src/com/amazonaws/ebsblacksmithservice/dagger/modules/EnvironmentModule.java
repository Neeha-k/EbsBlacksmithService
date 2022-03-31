package com.amazonaws.ebsblacksmithservice.dagger.modules;

import amazon.platform.config.AppConfig;
import amazon.platform.config.AppConfigTree;
import amazon.platform.config.Realm;
import amazon.platform.tools.ApolloEnvironmentInfo;
import amazon.platform.tools.ApolloEnvironmentInfo.EnvironmentRootUndefinedException;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.amazon.coral.dagger.config.ApolloServiceEnvironment;
import com.amazon.coral.dagger.config.ServiceEnvironment;
import com.amazon.coral.dagger.config.WorkspaceServiceEnvironment;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.coral.service.EnvironmentChecker;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import com.amazonaws.rip.RIPHelper;
import com.amazonaws.rip.models.IRIPHelper;
import com.amazonaws.rip.models.region.IRegion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Module()
public class EnvironmentModule {

    public static final String APP_NAME = "EbsBlacksmithService";
    private static final Logger log = LoggerFactory.getLogger(CoralModule.class);

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

    @Provides
    @Singleton
    @Named("httpPort")
    static int getHttpRegularPortFromOpConfigOrDefault() {
        return getHttpPortFromOpConfigOrDefault("httpRegularPort", 8080);
    }

    @Provides
    @Singleton
    @Named("Region")
    static IRegion provideRegion(IRIPHelper ripHelper, @Named("Realm") Realm realm) {
        return ripHelper.region(realm.name());
    }

    @Provides
    @Singleton
    @Named("httpSecurePort")
    static int getHttpSecurePortFromOpConfigOrDefault() {
        return getHttpPortFromOpConfigOrDefault("httpSecurePort", 8443);
    }

    @Provides
    @Singleton
    @Named("subZone")
    static String getSubZone(ServiceEnvironment serviceEnvironment) {
        return serviceEnvironment.getString("EBSZoneAndRegion", "EBSZoneAndRegion.subzone");
    }

    @Provides
    @Singleton
    @Named("IsIdm")
    public static Boolean isIdm(@Named("Domain") Domain domain) {
        return domain.toString().toLowerCase().equals("ec2");
    }

    private static int getHttpPortFromOpConfigOrDefault(String portName, int defaultPort) {
        Optional<Integer> apolloEnvironmentInfoPort =
                getApolloEnvironmentInfo()
                        .map(apolloEnvInfo -> apolloEnvInfo.getOperationalConfiguration("HttpServer"))
                        .map(httpServerMap -> httpServerMap.get(portName))
                        .map(portObject -> {
                            try {
                                return Integer.parseInt(portObject.toString());
                            } catch (NumberFormatException e) {
                                log.error("OpConfig value {} for HttpServer -> {} is not a number.", portObject, portName);
                                throw e;
                            }
                        });

        return apolloEnvironmentInfoPort.orElseGet(() -> {
            // Fallback to property set by Coral "brazil-build server" target
            String portProperty = System.getProperty("apollo.OCF.HttpServer." + portName, Integer.toString(defaultPort));
            log.warn("OpConfig value HttpServer -> {} not found. Using {}.", portName, portProperty);
            try {
                return Integer.parseInt(portProperty);
            } catch (NumberFormatException e) {
                log.error("System property apollo.OCF.HttpServer.{} was {}"
                        + " and could not be parsed as an integer.", portName, portProperty);
                throw e;
            }
        });
    }

    private static Optional<ApolloEnvironmentInfo> getApolloEnvironmentInfo() {
        try {
            return Optional.of(new ApolloEnvironmentInfo());
        } catch (EnvironmentRootUndefinedException | IOException e) {
            log.warn("Could not get ApolloEnvironmentInfo.", e);
            return Optional.empty();
        }
    }

    @Provides
    @Singleton
    static IRIPHelper provideRipHelper() {
        return RIPHelper.local();
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
