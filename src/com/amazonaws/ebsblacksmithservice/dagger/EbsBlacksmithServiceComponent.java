package com.amazonaws.ebsblacksmithservice.dagger;

import javax.inject.Singleton;

import com.amazon.coral.dagger.annotations.CoralComponent;
import com.amazon.coral.dagger.service.ActivityHandlerModule;

import com.amazonaws.ebsblacksmithservice.dagger.modules.AuthModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.CoralModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.CredentialsModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.DfddModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.EnvironmentModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.MetricsModule;
import com.amazonaws.ebsblacksmithservice.dagger.modules.PlacementModule;

@CoralComponent(
    modules = {
        EbsBlacksmithServiceManager.BindingModule.class,
        EnvironmentModule.class,
        MetricsModule.class,
        CredentialsModule.class,
        AuthModule.class,
        CoralModule.class,
        DfddModule.class,
        PlacementModule.class,
        ActivityHandlerModule.class,
    })
@Singleton
interface EbsBlacksmithServiceComponent {
}
