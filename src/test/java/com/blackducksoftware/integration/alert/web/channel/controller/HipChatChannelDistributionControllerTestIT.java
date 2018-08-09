package com.blackducksoftware.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.blackducksoftware.integration.alert.web.controller.ControllerTest;

public class HipChatChannelDistributionControllerTestIT extends ControllerTest<HipChatDistributionConfigEntity, HipChatDistributionConfig, HipChatDistributionRepository> {

    @Autowired
    HipChatDistributionRepository hipChatDistributionRepository;

    @Override
    public HipChatDistributionRepository getEntityRepository() {
        return hipChatDistributionRepository;
    }

    @Override
    public MockEntityUtil<HipChatDistributionConfigEntity> getEntityMockUtil() {
        return new MockHipChatEntity();
    }

    @Override
    public MockRestModelUtil<HipChatDistributionConfig> getRestModelMockUtil() {
        return new MockHipChatRestModel();
    }

    @Override
    public String getDescriptorName() {
        return HipChatChannel.COMPONENT_NAME;
    }

}