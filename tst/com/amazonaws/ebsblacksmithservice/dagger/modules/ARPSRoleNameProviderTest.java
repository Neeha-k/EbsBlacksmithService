package com.amazonaws.ebsblacksmithservice.dagger.modules;

import com.amazonaws.ebsblacksmithservice.types.Domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ARPSRoleNameProviderTest {
    @Test
    void testARPSRoleNameProvidedInIdmCorrectly() {
        Domain domain = Domain.EC2;
        CredentialsModule credentialsModule = new CredentialsModule();
        Assertions.assertEquals(credentialsModule.getARPSRoleName(domain, true), "EbsBlacksmithServiceARPSRole-v0-ec2");
    }

    @Test
    void testARPSRoleNameProvidedInGammaCorrectly() {
        Domain domain = Domain.GAMMA;
        CredentialsModule credentialsModule = new CredentialsModule();
        Assertions.assertEquals(credentialsModule.getARPSRoleName(domain, false), "EbsBlacksmithServiceARPSRole-v0" +
                "-gamma");
    }
}
