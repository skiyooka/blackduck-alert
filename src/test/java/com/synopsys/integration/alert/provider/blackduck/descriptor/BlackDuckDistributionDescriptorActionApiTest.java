package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckDistributionDescriptorActionApiTest {

    @Test
    public void testValidateConfig() {
        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi(Mockito.mock(ContentConverter.class));
        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
        final FieldModel fieldAccessor = Mockito.mock(FieldModel.class);
        Mockito.when(fieldAccessor.getField(Mockito.anyString())).thenReturn(Optional.empty());
        spiedRestApi.validateConfig(fieldAccessor, new HashMap<>());
        Mockito.verify(spiedRestApi).validateConfig(Mockito.any(), Mockito.anyMap());
    }

    @Test
    public void testTestConfig() throws IntegrationException {
        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi(Mockito.mock(ContentConverter.class));
        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
        spiedRestApi.testConfig(Mockito.any());
        Mockito.verify(spiedRestApi).testConfig(Mockito.any());
    }
}
