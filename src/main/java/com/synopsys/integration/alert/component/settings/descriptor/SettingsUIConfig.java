/**
 * blackduck-alert
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
package com.synopsys.integration.alert.component.settings.descriptor;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class SettingsUIConfig extends UIConfig {
    private static final String LABEL_ENCRYPTION_PASSWORD = "Encryption Password";
    private static final String LABEL_ENCRYPTION_GLOBAL_SALT = "Encryption Global Salt";
    private static final String LABEL_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE = "Startup Environment Variable Override";
    private static final String LABEL_PROXY_HOST = "Proxy Host";
    private static final String LABEL_PROXY_PORT = "Proxy Port";
    private static final String LABEL_PROXY_USERNAME = "Proxy Username";
    private static final String LABEL_PROXY_PASSWORD = "Proxy Password";

    private static final String SETTINGS_ENCRYPTION_PASSWORD_DESCRIPTION = "The password used when encrypting sensitive fields. Must be at least 8 characters long.";
    private static final String SETTINGS_ENCRYPTION_SALT_DESCRIPTION = "The salt used when encrypting sensitive fields. Must be at least 8 characters long.";
    private static final String SETTINGS_ENVIRONMENT_VARIABLE_OVERRIDE_DESCRIPTION = "If true, the Alert environment variables will override the stored configurations.";

    private static final String SETTINGS_PROXY_HOST_DESCRIPTION = "The host name of the proxy server to use.";
    private static final String SETTINGS_PROXY_PORT_DESCRIPTION = "The port of the proxy server to use.";
    private static final String SETTINGS_PROXY_USERNAME_DESCRIPTION = "If the proxy server requires authentication, the username to authenticate with the proxy server.";
    private static final String SETTINGS_PROXY_PASSWORD_DESCRIPTION = "If the proxy server requires authentication, the password to authenticate with the proxy server.";

    private static final String SETTINGS_PANEL_PROXY = "Proxy Configuration";

    private static final String SETTINGS_HEADER_ENCRYPTION = "Encryption Configuration";

    private final EncryptionValidator encryptionConfigValidator;
    private final EncryptionValidator encryptionFieldValidator;

    @Autowired
    public SettingsUIConfig() {
        super(SettingsDescriptor.SETTINGS_LABEL, SettingsDescriptor.SETTINGS_DESCRIPTION, SettingsDescriptor.SETTINGS_URL);
        this.encryptionConfigValidator = new EncryptionFieldsSetValidator();
        this.encryptionFieldValidator = new EncryptionFieldValidator();
    }

    @Override
    public List<ConfigField> createFields() {
        List<ConfigField> defaultPanelFields = createDefaultSettingsPanel();
        List<ConfigField> proxyPanelFields = createProxyPanel();

        List<List<ConfigField>> fieldLists = List.of(defaultPanelFields, proxyPanelFields);
        return fieldLists.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<ConfigField> createDefaultSettingsPanel() {
        ConfigField encryptionPassword = new PasswordConfigField(SettingsDescriptor.KEY_ENCRYPTION_PWD, LABEL_ENCRYPTION_PASSWORD, SETTINGS_ENCRYPTION_PASSWORD_DESCRIPTION, encryptionFieldValidator)
                                             .applyRequired(true)
                                             .applyValidationFunctions(this::minimumEncryptionFieldLength)
                                             .applyHeader(SETTINGS_HEADER_ENCRYPTION);
        ConfigField encryptionSalt = new PasswordConfigField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, LABEL_ENCRYPTION_GLOBAL_SALT, SETTINGS_ENCRYPTION_SALT_DESCRIPTION, encryptionFieldValidator)
                                         .applyRequired(true)
                                         .applyValidationFunctions(this::minimumEncryptionFieldLength)
                                         .applyHeader(SETTINGS_HEADER_ENCRYPTION);
        ConfigField environmentVariableOverride = new CheckboxConfigField(SettingsDescriptor.KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE, LABEL_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE, SETTINGS_ENVIRONMENT_VARIABLE_OVERRIDE_DESCRIPTION);
        return List.of(encryptionPassword, encryptionSalt, environmentVariableOverride);
    }

    private List<ConfigField> createProxyPanel() {
        ConfigField proxyHost = new TextInputConfigField(ProxyManager.KEY_PROXY_HOST, LABEL_PROXY_HOST, SETTINGS_PROXY_HOST_DESCRIPTION);
        ConfigField proxyPort = new NumberConfigField(ProxyManager.KEY_PROXY_PORT, LABEL_PROXY_PORT, SETTINGS_PROXY_PORT_DESCRIPTION);
        ConfigField proxyUsername = new TextInputConfigField(ProxyManager.KEY_PROXY_USERNAME, LABEL_PROXY_USERNAME, SETTINGS_PROXY_USERNAME_DESCRIPTION);
        ConfigField proxyPassword = new PasswordConfigField(ProxyManager.KEY_PROXY_PWD, LABEL_PROXY_PASSWORD, SETTINGS_PROXY_PASSWORD_DESCRIPTION, encryptionConfigValidator);
        proxyHost
            .applyPanel(SETTINGS_PANEL_PROXY)
            .applyRequiredRelatedField(proxyPort.getKey());
        proxyPort
            .applyPanel(SETTINGS_PANEL_PROXY)
            .applyRequiredRelatedField(proxyHost.getKey());
        proxyUsername
            .applyPanel(SETTINGS_PANEL_PROXY)
            .applyRequiredRelatedField(proxyHost.getKey())
            .applyRequiredRelatedField(proxyPassword.getKey());
        proxyPassword
            .applyPanel(SETTINGS_PANEL_PROXY)
            .applyRequiredRelatedField(proxyHost.getKey())
            .applyRequiredRelatedField(proxyUsername.getKey());
        return List.of(proxyHost, proxyPort, proxyUsername, proxyPassword);
    }

    private Collection<String> minimumEncryptionFieldLength(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        if (fieldToValidate.hasValues() && fieldToValidate.getValue().orElse("").length() < 8) {
            return List.of(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_FIELD_TOO_SHORT);
        }
        return List.of();
    }

    private class EncryptionFieldsSetValidator extends EncryptionValidator {
        @Override
        public Collection<String> apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
            Function<FieldValueModel, Boolean> fieldSetCheck = field -> field.hasValues() || field.isSet();
            boolean pwdFieldSet = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).map(fieldSetCheck).orElse(false);
            boolean saltFieldSet = fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).map(fieldSetCheck).orElse(false);
            if (pwdFieldSet && saltFieldSet) {
                return List.of();
            }
            return List.of(ConfigField.REQUIRED_FIELD_MISSING);
        }
    }

    private class EncryptionFieldValidator extends EncryptionValidator {
        @Override
        public Collection<String> apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
            if (fieldValueModel.containsNoData() && !fieldValueModel.isSet()) {
                return List.of(ConfigField.REQUIRED_FIELD_MISSING);
            }
            return List.of();
        }
    }

}
