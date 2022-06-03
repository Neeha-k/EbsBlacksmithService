package capacity;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.capacity.CapacityProvider;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CapacityCacheTest {
    @Mock
    CapacityProvider capacityProvider;

    @Mock
    ScheduledExecutorService schedulerMock;

    CapacityCache cache;


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(capacityProvider.loadServerData()).thenReturn(ImmutableList.of(MetalServerInternal.builder()
                .availableDisks(0)
                .ipAddress("127.0.0.0")
                .build()));
        cache = new CapacityCache(capacityProvider, schedulerMock);
        cache.update();
    }

    @Test
    void schedulerInvocation() {
        verify(schedulerMock, times(1)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void cacheHydratedAtCreation() {

        List<MetalServerInternal> metalServersInitial = cache.getMetalServers();
        Assertions.assertEquals(1, metalServersInitial.size());
    }

    @Test
    void updateReplacesCache() {

        List<MetalServerInternal> metalServersInitial = cache.getMetalServers();

        when(capacityProvider.loadServerData()).thenReturn(ImmutableList.of(MetalServerInternal.builder()
                        .availableDisks(1)
                        .ipAddress("127.1.1.1")
                .build()));
        cache.update();

        List<MetalServerInternal> metalServersFormer = cache.getMetalServers();
        Assertions.assertNotEquals(metalServersInitial, metalServersFormer);
    }
}
