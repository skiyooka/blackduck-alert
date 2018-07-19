package com.blackducksoftware.integration.alert.channel;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.alert.datasource.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.datasource.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.datasource.audit.AuditNotificationRepository;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.alert.event.AlertEvent;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.google.gson.Gson;

public class ChannelTemplateManagerTest {
    private Gson gson;
    private ContentConverter contentConverter;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson);
    }

    @Test
    public void testSendEvents() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepository auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepository.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepository, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final ProjectData projectData = new ProjectData(DigestTypeEnum.DAILY, "test", "version", Arrays.asList(), null);
        final DigestModel digestModel = new DigestModel(Arrays.asList(projectData));
        final ChannelEvent hipChatEvent = new ChannelEvent(HipChatChannel.COMPONENT_NAME, contentConverter.convertToString(digestModel), 1L);
        channelTemplateManager.sendEvents(Arrays.asList(hipChatEvent));
    }

    @Test
    public void testSendEventReturnsFalse() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepository auditEntryRepositoryWrapper = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepositoryWrapper.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepository auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepository.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepositoryWrapper, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final ChannelEvent slackEvent = new ChannelEvent(SlackChannel.COMPONENT_NAME, null, 1L);
        final boolean isFalse = channelTemplateManager.sendEvent(slackEvent);
        assertTrue(!isFalse);
    }

    @Test
    public void testNotAbstractChannelEvent() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepository auditEntryRepositoryWrapper = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepositoryWrapper.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepository auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepository.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepositoryWrapper, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final AlertEvent dbStoreEvent = new AlertEvent("", null);
        final boolean isTrue = channelTemplateManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
