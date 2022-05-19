package com.amazonaws.ebsblacksmithservice.scripts.describedisks;

import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.coral.metrics.NullMetricsFactory;
import com.amazon.ec2.gts.DeviceUsage;
import com.amazon.ec2.gunpowder.GunpowderClient;
import com.amazon.ec2.gunpowder.GunpowderClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.ebs.auth.TurtleAWSCredentialsProvider;
import com.amazonaws.ebs.metal.client.EbsMetalServerClient;
import com.amazonaws.ebs.metal.client.EbsMetalServerClientBuilder;
import com.amazonaws.ebs.metal.client.coap.MetalServerRequestSender;
import com.amazonaws.ebs.metal.client.gunpowder.GunpowderClientConfig;
import com.amazonaws.ebs.metal.client.gunpowder.GunpowderClientFactory;
import com.amazonaws.ebs.metal.client.gunpowder.GunpowderTokenServiceContext;
import com.amazonaws.ebs.metal.client.operations.DescribeDisksRequest;
import com.amazonaws.ebs.metal.client.operations.DescribeDisksResponse;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.credentials.adapter.V1V2AwsCredentialProviderAdapter;

import java.util.Arrays;
import java.util.Optional;

/**
 * This script is used for testing the connectivity between Control Plane and Data Plane.
 * This code will be deleted once testing is complete.
 */
@Slf4j
public class DescribeDisks {
    private static final String IP = "--ip=";

    private static final String DRY_RUN = "--dry-run";

    public static void main(String[] args) {
        log.info("DescribeDisks script ran with the args: " + Arrays.toString(args));

        AWSCredentialsProvider awsCredentialsProvider = getAWSCredentialsProvider();

        GunpowderClientConfig gunpowderClientConfig = getGunpowderClientConfig(awsCredentialsProvider);
        MetricsFactory nullMetricsFactory = new NullMetricsFactory();
        GunpowderClient gunpowderClient = new GunpowderClientFactory(nullMetricsFactory).getGunpowderClient(gunpowderClientConfig);

        EbsMetalServerClient client = new EbsMetalServerClientBuilder()
            .withCoapRequestSender(new MetalServerRequestSender(gunpowderClient, true))
            .build();

        DescribeDisksRequest describeDisksRequest = new DescribeDisksRequest(getHost(args), 443, "test-request-id");

        log.info("Sending request: " + describeDisksRequest + " to host " + getHost(args));

        if (isDryRun(args)) {
            log.info("Dry run enabled, not sending the request");
        } else {
            DescribeDisksResponse describeDisksResponse = client.newDescribeDisksCall().call(describeDisksRequest,
                nullMetricsFactory.newMetrics());
            log.info("Received response " + Arrays.toString(describeDisksResponse.getResponse().toArray()));
        }
    }

    private static String getHost(String[] args) {
        String host = null;
        for (String arg : args) {
            if (arg.contains(IP)) {
                host = arg.substring(IP.length());
            }
        }
        return host;
    }

    private static boolean isDryRun(String[] args) {
        for (String arg : args) {
            if (arg.contains(DRY_RUN)) {
                return true;
            }
        }
        return false;
    }

    private static AWSCredentialsProvider getAWSCredentialsProvider() {
        log.info("Creating turtle credential provider");
        AwsCredentialsProvider awsCredentialsProviderV2 = TurtleAWSCredentialsProvider.builder()
            .serviceName("EbsBlacksmithService")
            .domain("gamma")
            .roleName("EbsBlacksmithServiceARPSRole-v0-iad7-gamma")
            .build();

        return V1V2AwsCredentialProviderAdapter.adapt(awsCredentialsProviderV2);
    }

    private static GunpowderClientConfig getGunpowderClientConfig(AWSCredentialsProvider awsCredentialsProvider) {
        log.info("Creating GunpowderClientConfiguration");
        GunpowderClientConfiguration gunpowderClientConfiguration = new GunpowderClientConfiguration()
            .setGunpowderTokensEnabled(true)
            .setTokenUsageScope(DeviceUsage.TEST);

        log.info("Creating GunpowderTokenServiceContext");
        GunpowderTokenServiceContext gtsContext = GunpowderTokenServiceContext.builder()
            .domain("prod")
            .region("IAD")
            .zone("IAD7")
            .awsCredentialsProvider(awsCredentialsProvider)
            .build();

        log.info("Creating GunpowderClientConfig");
        return GunpowderClientConfig.builder()
            // serviceId doesn't matter for now
            // https://code.amazon.com/packages/EbsPlacementCommon/blobs/4fc34c91882317ba83e21f43b7590d2d3d17972d/--/src/com/amazon/ebs/placement/server/CoapEbsServerClientFactory.java#L86
            .serviceId(1)
            .gunpowderTokenServiceContext(Optional.of(gtsContext))
            .gunpowderClientConfiguration(Optional.of(gunpowderClientConfiguration))
            .build();
    }
}
