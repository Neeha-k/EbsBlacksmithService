package com.amazonaws.ebsblacksmithservice;

import com.amazonaws.ebsblacksmithservice.activity.GetPlacementForMetalVolumeActivity;
import com.amazonaws.ebsblacksmithservice.dagger.DaggerGeneratedCoralComponent;
import com.amazonaws.ebsblacksmithservice.dagger.GeneratedCoralComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class AbstractFunctionalTestCase {

    protected GetPlacementForMetalVolumeActivity activity;

    @BeforeEach
    public void initialize() throws Exception {

        System.setProperty("root", ".");
        final GeneratedCoralComponent generatedCoralComponent = DaggerGeneratedCoralComponent.create();
        this.activity = generatedCoralComponent.providerGetPlacementForMetalVolumeActivity();
        assertNotNull(this.activity);
    }
}
