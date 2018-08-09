/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.alert.workflow.scheduled;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.AboutReader;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.workflow.PhoneHome;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;
import com.blackducksoftware.integration.rest.connection.RestConnection;

@Component
public class PhoneHomeTask extends ScheduledTask {
    public static final String TASK_NAME = "phonehome";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PhoneHome phoneHome;
    private final BlackDuckProperties blackDuckProperties;
    private final AboutReader aboutReader;

    @Autowired
    public PhoneHomeTask(final TaskScheduler taskScheduler, final PhoneHome phoneHome, final BlackDuckProperties blackDuckProperties, final AboutReader aboutReader) {
        super(taskScheduler, TASK_NAME);
        this.phoneHome = phoneHome;
        this.blackDuckProperties = blackDuckProperties;
        this.aboutReader = aboutReader;
    }

    @Override
    public void run() {
        final Optional<RestConnection> optionalRestConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalRestConnection.isPresent()) {
            try (final RestConnection restConnection = optionalRestConnection.get()) {
                final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection);
                final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService();
                final PhoneHomeRequestBody.Builder builder = phoneHome.createPhoneHomeBuilder(phoneHomeService, aboutReader.getProductVersion());
                if (builder != null) {
                    phoneHome.addChannelMetaData(builder);
                    phoneHomeService.phoneHome(builder);
                }
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}