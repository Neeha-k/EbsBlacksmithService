package com.amazonaws.ebsblacksmithservice.auth;

import com.amazon.coral.security.Authority;
import com.amazon.ebs.auth.helpers.AuthWhitelistHelper;
import com.amazonaws.ebsblacksmithservice.dagger.modules.EnvironmentModule;
import com.amazonaws.ebsblacksmithservice.types.Domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AuthorizationList {

    private static final String SERVICE_NAME = EnvironmentModule.APP_NAME + "/";

    public static final String GET_PLACEMENT_FOR_METAL_VOLUMES = fullName("GetPlacementForMetalVolumes");

    public static Map<String, Set<String>> createApiToAuthorizedServicePrincipals(Domain domain) {
        Map<String, Set<String>> apiToAuthorizedServicePrincipals = new HashMap<>();

        switch (domain) {
            case DESKTOP: {
                break;
            }
            case EC2: {
                break;
            }
            case GAMMA: {
                break;
            }
            case PROD: {
                break;
            }
            default: {
                throw new RuntimeException("Invalid domain provided in the API Authorization List");
            }
        }
        return apiToAuthorizedServicePrincipals;
    }

    public static Map<String, Authority> getApiToAuthority(Domain domain) {
        return AuthWhitelistHelper.createApiToAuthorityMapWithApiToServicePrincipalsMap(
                createApiToAuthorizedServicePrincipals(domain)
        );
    }

    private static void authorize(Map<String, Set<String>> authorizationMap, String operation, String servicePrincipal) {
        if (authorizationMap.containsKey(operation)) {
            authorizationMap.get(operation).add(servicePrincipal);
        } else {
            authorizationMap.put(operation, Set.of(servicePrincipal));
        }
    }

    private static String fullName(String operation) {
        return SERVICE_NAME + operation;
    }
}
