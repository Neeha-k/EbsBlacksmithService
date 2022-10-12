package com.amazonaws.ebsblacksmithservice.placement;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlacementStrategyDecider {
    private static final Map<PlacementStrategyType, PlacementStrategy> PLACEMENT_STRATEGY_REGISTRY = new HashMap<>();

    public static void registerPlacementStrategy(
        final PlacementStrategyType strategyType,
        final PlacementStrategy placementStrategy) {
        PLACEMENT_STRATEGY_REGISTRY.put(strategyType, placementStrategy);
    }

    public static PlacementStrategy getPlacementStrategy(final PlacementOptions placementOptions) {

        if (placementOptions.hasTargetingOptionForPlacement()) {
            return PLACEMENT_STRATEGY_REGISTRY.get(PlacementStrategyType.TARGETING);
        }
        return PLACEMENT_STRATEGY_REGISTRY.get(PlacementStrategyType.RANDOM);
    }
}
