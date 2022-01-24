package auth;

import com.amazonaws.ebsblacksmithservice.auth.AuthorizationList;
import com.amazonaws.ebsblacksmithservice.types.Domain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthorizationListTest {

    @Test
    void desktopPermissions() {
        var apiToAuthority = AuthorizationList.getApiToAuthority(Domain.DESKTOP);
        Assertions.assertNotNull(apiToAuthority);
    }

}
