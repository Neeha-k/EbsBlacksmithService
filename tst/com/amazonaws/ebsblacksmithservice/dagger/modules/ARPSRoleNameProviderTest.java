package com.amazonaws.ebsblacksmithservice.dagger.modules;

import com.amazonaws.ebsblacksmithservice.dagger.modules.CredentialsModule;
import com.amazonaws.ebsblacksmithservice.types.Domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ARPSRoleNameProviderTest {
    @Test
    void testARPSRoleNameProvidedInIdmCorrectly() {
        String zone = "iad7";
        Domain domain = Domain.EC2;
        CredentialsModule credentialsModule = new CredentialsModule();
        Assertions.assertEquals(credentialsModule.getARPSRoleName(domain, zone, true), "EbsBlacksmithServiceARPSRole-v0-iad7-ec2");
    }

    @Test
    void testARPSRoleNameProvidedInGammaCorrectly() {
        String zone = "iad7";
        Domain domain = Domain.GAMMA;
        CredentialsModule credentialsModule = new CredentialsModule();
        Assertions.assertEquals(credentialsModule.getARPSRoleName(domain, zone, false), "EbsBlacksmithServiceARPSRole-v0-iad7-gamma");
    }
}
