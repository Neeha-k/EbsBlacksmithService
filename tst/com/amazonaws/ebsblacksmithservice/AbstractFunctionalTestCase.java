package com.amazonaws.ebsblacksmithservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.ebsblacksmithservice.activity.GetPlacementForMetalVolumeActivity;
import com.amazonaws.ebsblacksmithservice.dagger.DaggerGeneratedCoralComponent;
import com.amazonaws.ebsblacksmithservice.dagger.GeneratedCoralComponent;

@ExtendWith(MockitoExtension.class)
public class AbstractFunctionalTestCase {

    protected GetPlacementForMetalVolumeActivity activity;

    static GeneratedCoralComponent generatedCoralComponent;

    @BeforeAll
    static void initialize() throws Exception {
        System.setProperty("root", ".");
        generatedCoralComponent = DaggerGeneratedCoralComponent.create();
    }

    @BeforeEach
    public void setUp() {
        generatedCoralComponent.provideRandomizedPlacementStrategy();
        generatedCoralComponent.provideTargetingPlacementStrategy();
        this.activity = generatedCoralComponent.providerGetPlacementForMetalVolumeActivity();
        assertNotNull(this.activity);
    }
}
