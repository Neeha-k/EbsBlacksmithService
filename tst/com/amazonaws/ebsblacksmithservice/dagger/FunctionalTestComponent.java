package com.amazonaws.ebsblacksmithservice.dagger;


import com.amazon.coral.dagger.annotations.CoralComponent;
import com.amazon.coral.dagger.service.ActivityHandlerModule;
import com.amazonaws.ebsblacksmithservice.activity.GetPlacementForMetalVolumeActivity;
import com.amazonaws.ebsblacksmithservice.dagger.modules.CredentialsModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.DfddModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.PlacementModule;

import javax.inject.Singleton;

@Singleton
@CoralComponent(
        modules = {
                EbsBlacksmithServiceManager.BindingModule.class,
                TestEnvironmentModule.class,
                CredentialsModule.class,
                DfddModule.class,
                PlacementModule.class,
                ActivityHandlerModule.class
        },
        generateLauncher = false
)
public interface FunctionalTestComponent {

    GetPlacementForMetalVolumeActivity providerGetPlacementForMetalVolumeActivity();
}
