/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.mock.model;

import com.blackducksoftware.integration.hub.alert.mock.DistributionMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;
import com.google.gson.JsonObject;

public class MockHipChatRestModel extends MockRestModelUtil<HipChatDistributionRestModel> {
    private final DistributionMockUtils distributionMockUtil = new DistributionMockUtils("1");

    private final String roomId;
    private final String notify;
    private final String color;
    private final String id;

    public MockHipChatRestModel() {
        this("11", "false", "black", "1");
    }

    private MockHipChatRestModel(final String roomId, final String notify, final String color, final String id) {
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public HipChatDistributionRestModel createRestModel() {
        final HipChatDistributionRestModel restModel = new HipChatDistributionRestModel(distributionMockUtil.getCommonId(), roomId, notify, color, distributionMockUtil.getDistributionConfigId(), distributionMockUtil.getDistributionType(),
                distributionMockUtil.getName(), distributionMockUtil.getFrequency(), distributionMockUtil.getFilterByProject(), distributionMockUtil.getProjects(), distributionMockUtil.getNotifications());
        return restModel;
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("roomId", roomId);
        json.addProperty("notify", notify);
        json.addProperty("color", color);
        distributionMockUtil.getDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("roomId", null);
        json.add("notify", null);
        json.add("color", null);
        distributionMockUtil.getEmptyDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public HipChatDistributionRestModel createEmptyRestModel() {
        return new HipChatDistributionRestModel();
    }

}
