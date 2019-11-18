/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.common;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.issuetracker.IssueContentModel;
import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.issuetracker.message.IssueCommentRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueResolutionRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraMessageContentConverter {
    private static final Logger logger = LoggerFactory.getLogger(JiraMessageContentConverter.class);
    private JiraMessageParser jiraMessageParser;

    @Autowired
    public JiraMessageContentConverter(JiraMessageParser jiraMessageParser) {
        this.jiraMessageParser = jiraMessageParser;
    }

    public Collection<IssueTrackerRequest> convertMessageContents(IssueConfig issueConfig, MessageContentGroup content) throws IntegrationException {
        Collection<IssueTrackerRequest> issues = new LinkedList<>();
        for (ProviderMessageContent messageContent : content.getSubContent()) {
            Collection<IssueTrackerRequest> issueKeysForMessage = createOrUpdateIssuesPerComponent(issueConfig, messageContent);
            issues.addAll(issueKeysForMessage);
        }
        return issues;
    }

    protected Collection<IssueTrackerRequest> updateIssueByTopLevelAction(IssueConfig issueConfig, String providerName, LinkableItem topic, LinkableItem nullableSubTopic, ItemOperation action) throws IntegrationException {
        if (ItemOperation.DELETE == action) {
            logger.debug("Attempting to resolve issues in the project {} for Provider: {}, Provider Project: {}[{}].", issueConfig.getProjectKey(), providerName, topic.getValue(), nullableSubTopic);
            String trackingKey = createAdditionalTrackingKey(null);
            IssueTrackerRequest issueRequest = jiraMessageParser.createIssueContentModel(trackingKey, providerName, topic, nullableSubTopic, Set.of(), null);
            updateExistingIssues(issueConfig, providerName, topic.getName(), Set.of(), issueRequest);
            return List.of(issueRequest);
        } else {
            logger.debug("The top level action was not a DELETE action so it will be ignored");
        }
        return List.of();
    }

    protected Collection<IssueTrackerRequest> createOrUpdateIssuesByComponentGroup(IssueConfig issueConfig, String providerName, LinkableItem topic, LinkableItem nullableSubTopic, SetMap<String, ComponentItem> groupedComponentItems)
        throws IntegrationException {
        Collection<IssueTrackerRequest> issues = new LinkedList<>();

        SetMap<String, String> missingTransitionToIssues = SetMap.createDefault();
        for (Set<ComponentItem> componentItems : groupedComponentItems.values()) {
            try {
                ComponentItem arbitraryItem = componentItems
                                                  .stream()
                                                  .findAny()
                                                  .orElseThrow(() -> new AlertException(String.format("No actionable component items were found. Provider: %s, Topic: %s, SubTopic: %s", providerName, topic, nullableSubTopic)));
                ItemOperation operation = arbitraryItem.getOperation();
                String trackingKey = createAdditionalTrackingKey(arbitraryItem);
                IssueTrackerRequest issueRequest = jiraMessageParser.createIssueContentModel(trackingKey, providerName, topic, nullableSubTopic, componentItems, arbitraryItem);
                if (ItemOperation.DELETE == operation || ItemOperation.INFO == operation) {
                    // keep the properties only add the comments.
                    IssueContentModel issueContent = IssueContentModel.of(StringUtils.EMPTY, StringUtils.EMPTY, new LinkedList<>());
                    if (ItemOperation.DELETE == operation) {
                        issueRequest = IssueResolutionRequest.of(issueRequest.getIssueProperties(), issueContent);
                    }
                    if (ItemOperation.INFO == operation) {
                        issueRequest = IssueCommentRequest.of(issueRequest.getIssueProperties(), issueContent);
                    }
                    updateExistingIssues(issueConfig, providerName, topic.getName(), componentItems, issueRequest);
                }
                issues.add(issueRequest);
            } catch (IssueMissingTransitionException e) {
                missingTransitionToIssues.add(e.getTransition(), e.getIssueKey());
            }
        }
        return issues;
    }

    protected String createAdditionalTrackingKey(ComponentItem componentItem) {
        if (null != componentItem && !componentItem.collapseOnCategory()) {
            LinkableItem categoryItem = componentItem.getCategoryItem();
            return categoryItem.getName() + categoryItem.getValue();
        }
        return StringUtils.EMPTY;
    }

    protected void updateExistingIssues(IssueConfig issueConfig, String providerName, String category, Set<ComponentItem> componentItems, IssueTrackerRequest issueTrackerRequest)
        throws IntegrationException {
        if (issueConfig.getCommentOnIssues()) {
            List<String> operationComments = jiraMessageParser.createOperationComment(providerName, category, issueTrackerRequest.getOperation(), componentItems);
            issueTrackerRequest.getRequestContent().getAdditionalComments().addAll(operationComments);
        }
    }

    private Collection<IssueTrackerRequest> createOrUpdateIssuesPerComponent(IssueConfig issueConfig, ProviderMessageContent messageContent) throws IntegrationException {
        String providerName = messageContent.getProvider().getValue();
        LinkableItem topic = messageContent.getTopic();
        LinkableItem nullableSubTopic = messageContent.getSubTopic().orElse(null);

        Collection<IssueTrackerRequest> requests;
        if (messageContent.isTopLevelActionOnly()) {
            requests = updateIssueByTopLevelAction(issueConfig, providerName, topic, nullableSubTopic, messageContent.getAction().orElse(ItemOperation.INFO));
        } else {
            requests = createOrUpdateIssuesByComponentGroup(issueConfig, providerName, topic, nullableSubTopic, messageContent.groupRelatedComponentItems());
        }
        return requests;
    }

}
