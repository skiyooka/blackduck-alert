/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.provider.lifecycle;

import java.util.List;

import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskMetaData;
import com.synopsys.integration.alert.common.workflow.task.TaskMetaDataProperty;

public abstract class ProviderTask extends ScheduledTask {
    private ProviderProperties providerProperties;
    private ProviderKey providerKey;
    private String taskName;

    public ProviderTask(ProviderKey providerKey, TaskScheduler taskScheduler, ProviderProperties providerProperties) {
        super(taskScheduler);
        this.providerKey = providerKey;
        this.providerProperties = providerProperties;
        this.taskName = computeProviderTaskName(providerKey, getProviderProperties().getConfigId(), getClass());
    }

    @Override
    public final void runTask() {
        runProviderTask();
    }

    public abstract void runProviderTask();

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public TaskMetaData createTaskMetaData() {
        String fullyQualifiedName = ScheduledTask.computeFullyQualifiedName(getClass());
        String nextRunTime = getFormatedNextRunTime().orElse("");
        String providerName = providerKey.getDisplayName();
        String configName = providerProperties.getConfigName();
        TaskMetaDataProperty providerProperty = new TaskMetaDataProperty("provider", "Provider", providerName);
        TaskMetaDataProperty configurationProperty = new TaskMetaDataProperty("configurationName", "Configuration Name", configName);
        List<TaskMetaDataProperty> properties = List.of(providerProperty, configurationProperty);

        return new TaskMetaData(getTaskName(), getClass().getSimpleName(), fullyQualifiedName, nextRunTime, properties);

    }

    protected ProviderProperties getProviderProperties() {
        return providerProperties;
    }

    private String computeProviderTaskName(ProviderKey providerKey, Long providerConfigId, Class<? extends ProviderTask> providerTaskClass) {
        String superTaskName = ScheduledTask.computeTaskName(providerTaskClass);
        String providerUniversalKey = providerKey.getUniversalKey();

        return String.format("%s::Provider[%s]::Configuration[id:%d]", superTaskName, providerUniversalKey, providerConfigId);
    }

}
