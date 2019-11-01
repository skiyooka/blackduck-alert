/**
 * alert-common
 * <p>
 * Copyright (c) 2019 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

import java.util.*;

public interface ProviderDataAccessor {
    Optional<ProviderProject> findFirstByHref(String href);

    Optional<ProviderProject> findFirstByName(String name);

    List<ProviderProject> findByProviderName(String providerName);

    ProviderProject saveProject(String providerName, ProviderProject providerProject);

    List<ProviderProject> saveProjects(String providerName, Collection<ProviderProject> providerProjects);

    void deleteProjects(String providerName, Collection<ProviderProject> providerProjects);

    void deleteByHref(String projectHref);

    Set<String> getEmailAddressesForProjectHref(String projectHref);

    void mapUsersToProjectByEmail(String projectHref, Collection<String> emailAddresses) throws AlertDatabaseConstraintException;

    List<ProviderUserModel> getAllUsers(String providerName);

    AlertPagedModel<ProviderUserModel> getPageOfUsers(String providerName, Integer offset, Integer limit, String q) throws AlertDatabaseConstraintException;

    List<ProviderUserModel> saveUsers(String providerName, Collection<ProviderUserModel> users);

    void deleteUsers(String providerName, Collection<ProviderUserModel> users);

    void updateProjectAndUserData(String providerName, Map<ProviderProject, Set<String>> projectToUserData);
}
