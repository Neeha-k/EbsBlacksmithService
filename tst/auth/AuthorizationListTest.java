package auth;

import com.amazonaws.ebsblacksmithservice.auth.AuthorizationList;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import com.amazonaws.rip.RIPHelper;
import com.amazonaws.rip.models.IRIPHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class AuthorizationListTest {

    @Test
    void desktopPermissions() {
        IRIPHelper iripHelper = RIPHelper.local();
        Map<String, List<String>> AuthorizedServicePrincipalsToApis =
                AuthorizationList.createAuthorizedServicePrincipalToApisMap(
                Domain.DESKTOP,
                "iad7",
                iripHelper.region("iad")
        );
        Assertions.assertNotNull(AuthorizedServicePrincipalsToApis);
    }

    @Test
    void kapowPermissions() {
        IRIPHelper iripHelper = RIPHelper.local();
        Map<String, List<String>> AuthorizedServicePrincipalsToApis =
                AuthorizationList.createAuthorizedServicePrincipalToApisMap(
                Domain.GAMMA,
                "iad7",
                iripHelper.region("iad")
        );
        List<String> gammaServicePrincipalsAllowedApis = new ArrayList<>();
        gammaServicePrincipalsAllowedApis.add(AuthorizationList.GET_PLACEMENT_FOR_METAL_VOLUMES);
        Assertions.assertEquals(gammaServicePrincipalsAllowedApis,
                AuthorizedServicePrincipalsToApis.get(AuthorizationList.EBS_KAPOW_GAMMA_SERVICE_PRINCIPAL));

        AuthorizedServicePrincipalsToApis = AuthorizationList.createAuthorizedServicePrincipalToApisMap(
                Domain.PROD,
                "iad7",
                iripHelper.region("iad")
        );
        List<String> prodServicePrincipalsAllowedApis = new ArrayList<>();
        prodServicePrincipalsAllowedApis.add(AuthorizationList.GET_PLACEMENT_FOR_METAL_VOLUMES);
        Assertions.assertEquals(prodServicePrincipalsAllowedApis,
                AuthorizedServicePrincipalsToApis.get("iad7.iad.prod.ebs-kapow.aws.internal"));
    }

    /**
     * This test is to make sure it can add >1 operation to the same service principal without failing.
     */
    @Test
    void authorizeTest() {
        Map<String, List<String>> authorizedServicePrincipalsToApis = new HashMap<>();
        String testOperation = "EbsBlacksmithService/TestOperation";
        AuthorizationList.authorize(authorizedServicePrincipalsToApis,
                AuthorizationList.GET_PLACEMENT_FOR_METAL_VOLUMES,
                AuthorizationList.EBS_KAPOW_GAMMA_SERVICE_PRINCIPAL);
        AuthorizationList.authorize(authorizedServicePrincipalsToApis,
                testOperation,
                AuthorizationList.EBS_KAPOW_GAMMA_SERVICE_PRINCIPAL);
        List<String> gammaServicePrincipalsAllowedApis = new ArrayList<>();
        gammaServicePrincipalsAllowedApis.add(AuthorizationList.GET_PLACEMENT_FOR_METAL_VOLUMES);
        gammaServicePrincipalsAllowedApis.add(testOperation);
        Assertions.assertEquals(gammaServicePrincipalsAllowedApis,
                authorizedServicePrincipalsToApis.get(AuthorizationList.EBS_KAPOW_GAMMA_SERVICE_PRINCIPAL));
    }

}
