/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.accumulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class AccumulatorProcessor implements ItemProcessor<NotificationResults, DBStoreEvent> {
    private final Logger logger = LoggerFactory.getLogger(AccumulatorProcessor.class);
    private final GlobalProperties globalProperties;

    public AccumulatorProcessor(final GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Override
    public DBStoreEvent process(final NotificationResults notificationData) throws Exception {
        try {
            final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryAndLogErrors(logger);
            if (hubServicesFactory != null) {
                final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(hubServicesFactory.createProjectRequestService(), hubServicesFactory.createProjectAssignmentRequestService(),
                        hubServicesFactory.createHubResponseService(), hubServicesFactory.createVulnerabilityRequestService(), new MetaService(new Slf4jIntLogger(logger)));
                final DBStoreEvent storeEvent = notificationItemProcessor.process(notificationData.getNotificationContentItems());
                return storeEvent;
            }
        } catch (final Exception ex) {
            logger.error("Error occurred durring processing of accumulated notifications", ex);
        }
        return null;
    }
}
