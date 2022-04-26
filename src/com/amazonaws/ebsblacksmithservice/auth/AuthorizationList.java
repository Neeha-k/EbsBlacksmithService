package com.amazonaws.ebsblacksmithservice.auth;
import com.amazon.coral.security.Authority;
import com.amazon.ebs.auth.helpers.AuthWhitelistHelper;
import com.amazonaws.ebsblacksmithservice.dagger.modules.EnvironmentModule;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import com.amazonaws.rip.models.region.IRegion;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthorizationList {

    private static final String SERVICE_NAME = EnvironmentModule.APP_NAME + "/";
    private static final String EBS_KAPOW_PROD_SERVICE_PRINCIPAL = "%s.%s.prod.ebs-kapow.aws.internal";

    public static final String EBS_KAPOW_GAMMA_SERVICE_PRINCIPAL = "gamma.ebs-kapow.aws.internal";

    public static final String GET_PLACEMENT_FOR_METAL_VOLUMES = fullName("GetPlacementForMetalVolumes");

    /**
     *
     * @param domain
     * @param zone
     * @param region
     * @return A map with following manner: service principal -> List<APIs allowed for service principal>
     */
    public static Map<String, List<String>> createAuthorizedServicePrincipalToApisMap(
            Domain domain,
            String zone,
            IRegion region) {
        Map<String, List<String>> authorizedServicePrincipalsToApis = new HashMap<>();

        switch (domain) {
            case DESKTOP: {
                break;
            }
            case EC2: {
                break;
            }
            case GAMMA: {
                authorize(authorizedServicePrincipalsToApis,
                        GET_PLACEMENT_FOR_METAL_VOLUMES,
                        EBS_KAPOW_GAMMA_SERVICE_PRINCIPAL);
                break;
            }
            case PROD: {
                authorize(authorizedServicePrincipalsToApis,
                        GET_PLACEMENT_FOR_METAL_VOLUMES,
                        servicePrincipalFormatter(EBS_KAPOW_PROD_SERVICE_PRINCIPAL, zone, region));
                break;
            }
            default: {
                throw new RuntimeException("Invalid domain provided in the API Authorization List");
            }
        }
        return authorizedServicePrincipalsToApis;
    }

    /**
     * Reversed service principal -> List<APIs allowed for service principal> Map
     * @param domain
     * @param zone
     * @param region
     * @return A map with following manner: API -> List<service principals allowed for this API>
     */
    public static Map<String, Authority> getApiToAuthority(
            Domain domain,
            String zone,
            IRegion region) {
        return AuthWhitelistHelper.createApiToAuthorityMapWithApiToServicePrincipalsMap(
                AuthWhitelistHelper.createApiToServicePrincipalsMap(
                        createAuthorizedServicePrincipalToApisMap(domain, zone, region))
        );
    }

    @VisibleForTesting
    protected static void authorize(Map<String, List<String>> authorizationMap,
                                  String operation, String servicePrincipal) {
        if (authorizationMap.containsKey(servicePrincipal)) {
            authorizationMap.get(servicePrincipal).add(operation);
        } else {
            List<String> operationList = new ArrayList<>();
            operationList.add(operation);
            authorizationMap.put(servicePrincipal, operationList);
        }
    }

    private static String fullName(String operation) {
        return SERVICE_NAME + operation;
    }

    private static String servicePrincipalFormatter(String servicePrincipal, String zone, IRegion region){
        return String.format(servicePrincipal, zone, region.airportCode().toLowerCase());
    }
}
