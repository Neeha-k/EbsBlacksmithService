package com.amazonaws.ebsblacksmithservice.dagger.modules;

import static com.amazon.ebs.dfdd.EbsAppNames.secure;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.inject.Named;
import javax.inject.Singleton;
import com.amazon.ebs.dfdd.DfddHealthChecker;
import com.amazon.ebs.dfdd.DfddHeartbeatHelper;
import com.amazon.ebs.dfdd.DfddHeartbeater;
import com.amazon.ebs.dfdd.EbsAppNames;
import dagger.Module;
import dagger.Provides;
import org.apache.commons.httpclient.URIException;

@Module
public class DfddModule {

    private static final String HTTP_REGULAR_PORT = "httpPort";
    private static final String HTTP_SECURE_PORT = "httpSecurePort";

    @Provides
    @Singleton
    @Named("DfddHealthCheckerRegular")
    static DfddHealthChecker getDfddHealthCheckerRegular(@Named(HTTP_REGULAR_PORT) int httpRegularPort) {
        try {
            return new DfddHealthChecker(httpRegularPort);
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    @Named("DfddHealthCheckerSecure")
    static DfddHealthChecker getDfddHealthCheckerSecure(@Named(HTTP_SECURE_PORT) int httpSecurePort) {
        try {
            String ipAddr = InetAddress.getLocalHost().getHostAddress();
            String url = String.format("https://%s:%d/ping", ipAddr, httpSecurePort);
            return new DfddHealthChecker(url);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }



    @Provides
    @Singleton
    @Named("DfddHeartbeatHelperRegular")
    static DfddHeartbeatHelper getRegularDfddHeartbeatHelper(@Named(HTTP_REGULAR_PORT) int httpRegularPort,
            @Named("DfddHealthCheckerRegular") DfddHealthChecker healthChecker) {
        try {
            return new DfddHeartbeatHelper(
                    EbsAppNames.EBS_BLACKSMITH_SERVICE, httpRegularPort, healthChecker,
                    new DfddHeartbeater());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    @Named("DfddHeartbeatHelperSecure")
    static DfddHeartbeatHelper getSecureDfddHeartbeatHelper(@Named(HTTP_SECURE_PORT) int httpSecurePort,
            @Named("DfddHealthCheckerSecure") DfddHealthChecker healthChecker) {
        try {
            return new DfddHeartbeatHelper(
                    secure(EbsAppNames.EBS_BLACKSMITH_SERVICE), httpSecurePort, healthChecker,
                    new DfddHeartbeater());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
