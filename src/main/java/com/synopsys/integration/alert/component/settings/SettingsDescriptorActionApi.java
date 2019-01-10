/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.component.settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SettingsDescriptorActionApi extends DescriptorActionApi {
    private static final Logger logger = LoggerFactory.getLogger(SettingsDescriptorActionApi.class);
    private final EncryptionUtility encryptionUtility;
    private final UserAccessor userAccessor;
    private final SystemValidator systemValidator;

    @Autowired
    public SettingsDescriptorActionApi(final EncryptionUtility encryptionUtility, final UserAccessor userAccessor, final SystemValidator systemValidator) {
        this.encryptionUtility = encryptionUtility;
        this.userAccessor = userAccessor;
        this.systemValidator = systemValidator;
    }

    @Override
    public void validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        final Optional<FieldValueModel> defaultUserPassword = fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD);
        final Optional<FieldValueModel> encryptionPassword = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD);
        final Optional<FieldValueModel> encryptionSalt = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

        validateField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, fieldModel, (valueModel) -> {
            if (StringUtils.isBlank(valueModel.getValue().orElse(""))) {
                fieldErrors.put(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_PASSWORD);
            }
            return null;
        });

        validateField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, fieldModel, (valueModel) -> {
            if (StringUtils.isBlank(valueModel.getValue().orElse(""))) {
                fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PASSWORD);
            }
            return null;
        });

        validateField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, fieldModel, (valueModel) -> {
            if (StringUtils.isBlank(valueModel.getValue().orElse(""))) {
                fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT);
            }
            return null;
        });

        //        if (defaultUserPassword.isPresent()) {
        //            final FieldValueModel valueModel = defaultUserPassword.get();
        //            final boolean validate = valueModel.isSet() == false || (valueModel.isSet() && valueModel.hasValues());
        //            if (validate && StringUtils.isBlank(valueModel.getValue().orElse(""))) {
        //                fieldErrors.put(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_PASSWORD);
        //            }
        //        }
        //        if (encryptionPassword.isPresent()) {
        //            final FieldValueModel valueModel = encryptionPassword.get();
        //            final boolean validate = valueModel.isSet() == false || (valueModel.isSet() && valueModel.hasValues());
        //            if (validate && StringUtils.isBlank(valueModel.getValue().orElse(""))) {
        //                fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PASSWORD);
        //            }
        //        }
        //        if (encryptionSalt.isPresent()) {
        //            final FieldValueModel valueModel = encryptionSalt.get();
        //            final boolean validate = valueModel.isSet() == false || (valueModel.isSet() && valueModel.hasValues());
        //            if (validate && StringUtils.isBlank(valueModel.getValue().orElse(""))) {
        //                fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT);
        //            }
        //        }
    }

    private void validateField(final String fieldKey, final FieldModel fieldModel, final Function<FieldValueModel, Void> validationFunction) {
        final Optional<FieldValueModel> optionalField = fieldModel.getField(fieldKey);
        if (optionalField.isPresent()) {
            final FieldValueModel valueModel = optionalField.get();
            final boolean validateField = !valueModel.isSet() || valueModel.hasValues();
            if (validateField) {
                validationFunction.apply(valueModel);
            }
        }
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {

    }

    @Override
    public void readConfig(final FieldModel fieldModel) {
        final Optional<UserModel> defaultUser = userAccessor.getUser("sysadmin");
        final boolean defaultUserPasswordSet = defaultUser.isPresent() && StringUtils.isNotBlank(defaultUser.get().getPassword());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(null, defaultUserPasswordSet));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, encryptionUtility.isPasswordSet()));
    }

    @Override
    public FieldModel updateConfig(final FieldModel fieldModel) {
        saveDefaultAdminUserPassword(fieldModel);
        saveEncryptionProperties(fieldModel);
        return createScrubbedModel(fieldModel);
    }

    @Override
    public FieldModel saveConfig(final FieldModel fieldModel) {
        saveDefaultAdminUserPassword(fieldModel);
        saveEncryptionProperties(fieldModel);
        systemValidator.validate();
        return createScrubbedModel(fieldModel);
    }

    private FieldModel createScrubbedModel(final FieldModel fieldModel) {
        final HashMap<String, FieldValueModel> fields = new HashMap<>();
        fields.putAll(fieldModel.getKeyToValues());
        fields.remove(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD);

        fields.remove(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD);
        fields.remove(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);
        final FieldModel modelToSave = new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(), fields);
        return modelToSave;
    }

    private void saveDefaultAdminUserPassword(final FieldModel fieldModel) {
        final Optional<FieldValueModel> optionalPassword = fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD);
        if (optionalPassword.isPresent()) {
            final String password = optionalPassword.get().getValue().orElse("");
            if (StringUtils.isNotBlank(password)) {
                userAccessor.changeUserPassword(UserAccessor.DEFAULT_ADMIN_USER, password);
            }
        }
    }

    private void saveEncryptionProperties(final FieldModel fieldModel) {
        try {
            final Optional<FieldValueModel> optionalEncryptionPassword = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD);
            final Optional<FieldValueModel> optionalEncryptionSalt = fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

            if (optionalEncryptionPassword.isPresent()) {
                final String passwordToSave = optionalEncryptionPassword.get().getValue().orElse("");
                if (StringUtils.isNotBlank(passwordToSave)) {
                    encryptionUtility.updatePasswordField(passwordToSave);
                }
            }

            if (optionalEncryptionSalt.isPresent()) {
                final String saltToSave = optionalEncryptionSalt.get().getValue().orElse("");
                if (StringUtils.isNotBlank(saltToSave)) {
                    encryptionUtility.updateSaltField(saltToSave);
                }
            }
        } catch (final IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration.", ex);
        }
    }
}
