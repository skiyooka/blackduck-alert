package com.blackducksoftware.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.scheduling.TaskScheduler;

import com.blackducksoftware.integration.alert.TestAlertProperties;
import com.blackducksoftware.integration.alert.TestBlackDuckProperties;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.digest.DateRange;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.mock.entity.MockNotificationContent;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.policy.PolicyNotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.vulnerability.VulnerabilityNotificationTypeProcessor;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationViewResults;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.google.gson.Gson;

public class BlackDuckAccumulatorTest {

    private Gson gson;
    private ContentConverter contentConverter;

    private File testAccumulatorParent;

    private TestAlertProperties testAlertProperties;
    private TestBlackDuckProperties testBlackDuckProperties;
    private NotificationManager notificationManager;
    private TaskScheduler taskScheduler;

    @Before
    public void init() throws Exception {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        testAccumulatorParent = new File("testAccumulatorDirectory");
        testAccumulatorParent.mkdirs();
        System.out.println(testAccumulatorParent.getCanonicalPath());

        testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertConfigHome(testAccumulatorParent.getCanonicalPath());
        testBlackDuckProperties = new TestBlackDuckProperties(testAlertProperties);

        notificationManager = Mockito.mock(NotificationManager.class);
        taskScheduler = Mockito.mock(TaskScheduler.class);
    }

    @After
    public void cleanup() throws Exception {
        FileUtils.deleteDirectory(testAccumulatorParent);
    }

    private BlackDuckAccumulator createNonProcessingAccumulator(final BlackDuckProperties blackDuckProperties) {
        return createAccumulator(blackDuckProperties, Collections.emptyList());
    }

    private BlackDuckAccumulator createAccumulator(final BlackDuckProperties blackDuckProperties, final List<NotificationTypeProcessor> processorList) {
        return new BlackDuckAccumulator(taskScheduler, testAlertProperties, blackDuckProperties, contentConverter, notificationManager, processorList);
    }

    @Test
    public void testFormatDate() {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        final Date date = new Date();
        assertEquals(RestConnection.formatDate(date), notificationAccumulator.formatDate(date));
    }

    @Test
    public void testCreateDateRange() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        final ZonedDateTime startTime = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneOffset.UTC);
        final ZonedDateTime endTime = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneOffset.UTC);
        assertNotEquals(dateRange.getStart(), dateRange.getEnd());
        final ZonedDateTime expectedStartTime = endTime.minusMinutes(1);
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testCreateDateRangeIOException() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        final Date expectedStartDate = Date.from(startDateTime.toInstant());
        final String startString = notificationAccumulator.formatDate(expectedStartDate);
        FileUtils.write(notificationAccumulator.getSearchRangeFilePath(), startString, BlackDuckAccumulator.ENCODING);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("Can't read file test exception")).when(spiedAccumulator).readSearchStartTime(Mockito.any());
        final DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void testCreateDateRangeParseException() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        final Date expectedStartDate = Date.from(startDateTime.toInstant());
        final String startString = notificationAccumulator.formatDate(expectedStartDate);
        FileUtils.write(notificationAccumulator.getSearchRangeFilePath(), startString, BlackDuckAccumulator.ENCODING);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new ParseException("Can't parse date test exception", 1)).when(spiedAccumulator).parseDateString(Mockito.any());
        final DateRange dateRange = spiedAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void testCreateDateRangeWithExistingFile() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(testBlackDuckProperties);
        ZonedDateTime startDateTime = ZonedDateTime.now();
        startDateTime = startDateTime.withZoneSameInstant(ZoneOffset.UTC);
        startDateTime = startDateTime.withSecond(0).withNano(0);
        startDateTime = startDateTime.minusMinutes(5);
        final Date expectedStartDate = Date.from(startDateTime.toInstant());
        final String startString = notificationAccumulator.formatDate(expectedStartDate);
        FileUtils.write(notificationAccumulator.getSearchRangeFilePath(), startString, BlackDuckAccumulator.ENCODING);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        assertNotNull(dateRange);
        final Date actualStartDate = dateRange.getStart();
        final Date actualEndDate = dateRange.getEnd();
        assertEquals(expectedStartDate, actualStartDate);
        assertNotEquals(actualStartDate, actualEndDate);
    }

    @Test
    public void testRun() throws Exception {
        final List<NotificationTypeProcessor> processorList = Collections.emptyList();

        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, contentConverter, notificationManager, processorList);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.run();
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulate() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, contentConverter, notificationManager, Collections.emptyList());
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        assertTrue(spiedAccumulator.getSearchRangeFilePath().exists());
        Mockito.verify(spiedAccumulator, Mockito.times(2)).formatDate(Mockito.any());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateException() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, contentConverter, notificationManager, Collections.emptyList());
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.doThrow(new IOException("can't write last search file")).when(spiedAccumulator).saveNextSearchStart(Mockito.anyString());
        spiedAccumulator.accumulate();
        assertTrue(spiedAccumulator.getSearchRangeFilePath().exists());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateGetNextRunHasValue() throws Exception {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, contentConverter, notificationManager, Collections.emptyList());
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        Mockito.when(spiedAccumulator.getMillisecondsToNextRun()).thenReturn(Optional.of(Long.MAX_VALUE));
        spiedAccumulator.accumulate();
        assertTrue(spiedAccumulator.getSearchRangeFilePath().exists());
        Mockito.verify(spiedAccumulator).initializeSearchRangeFile();
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).accumulate(Mockito.any());
    }

    @Test
    public void testAccumulateWithDateRange() throws Exception {
        // this is the most comprehensive test as it mocks all services in use and completes the full process
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory blackDuckServicesFactory = Mockito.mock(HubServicesFactory.class);
        final HubService blackDuckService = Mockito.mock(HubService.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final HubBucketService bucketService = Mockito.mock(HubBucketService.class);

        final List<NotificationTypeProcessor> processorList = Collections.emptyList();

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService(Mockito.anyBoolean());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.when(blackDuckServicesFactory.createHubService()).thenReturn(blackDuckService);
        Mockito.when(blackDuckServicesFactory.createHubBucketService()).thenReturn(bucketService);

        final NotificationView notificationView = new NotificationView();
        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        notificationView.json = "{ content: \"content is here...\"}";
        final CommonNotificationView commonNotificationView = new CommonNotificationView(notificationView);
        final List<CommonNotificationView> viewList = Arrays.asList(commonNotificationView);
        final CommonNotificationViewResults viewResults = new CommonNotificationViewResults(viewList, Optional.of(notificationView.createdAt), Optional.of(RestConnection.formatDate(notificationView.createdAt)));
        Mockito.doReturn(viewResults).when(notificationService).getAllCommonNotificationViewResults(Mockito.any(), Mockito.any());

        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, mockedBlackDuckProperties, contentConverter, notificationManager, processorList);
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        final DateRange dateRange = spiedAccumulator.createDateRange(spiedAccumulator.getSearchRangeFilePath());
        spiedAccumulator.accumulate(dateRange);
        Mockito.verify(spiedAccumulator).createDateRange(Mockito.any());
        Mockito.verify(spiedAccumulator).read(Mockito.any());
        Mockito.verify(spiedAccumulator).process(Mockito.any());
        Mockito.verify(spiedAccumulator).write(Mockito.any());
    }

    @Test
    public void testAccumulateNextRunEmpty() {
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, contentConverter, notificationManager, Collections.emptyList());
        final BlackDuckAccumulator spiedAccumulator = Mockito.spy(notificationAccumulator);
        spiedAccumulator.accumulate();
        Mockito.verify(spiedAccumulator).getMillisecondsToNextRun();
    }

    @Test
    public void testRead() throws Exception {
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory blackDuckServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        final NotificationView notificationView = new NotificationView();
        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        notificationView.json = "{ content: \"content is here...\"}";
        final CommonNotificationView commonNotificationView = new CommonNotificationView(notificationView);
        final List<CommonNotificationView> viewList = Arrays.asList(commonNotificationView);
        final CommonNotificationViewResults viewResults = new CommonNotificationViewResults(viewList, Optional.of(notificationView.createdAt), Optional.of(RestConnection.formatDate(notificationView.createdAt)));
        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService(Mockito.anyBoolean());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(viewResults).when(notificationService).getAllCommonNotificationViewResults(Mockito.any(), Mockito.any());

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertTrue(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadNoNotifications() throws Exception {
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory blackDuckServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);

        final NotificationView notificationView = new NotificationView();
        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        notificationView.json = "{ content: \"content is here...\"}";
        final CommonNotificationView commonNotificationView = new CommonNotificationView(notificationView);
        final List<CommonNotificationView> viewList = Arrays.asList(commonNotificationView);
        final CommonNotificationViewResults viewResults = new CommonNotificationViewResults(viewList, Optional.of(notificationView.createdAt), Optional.of(RestConnection.formatDate(notificationView.createdAt)));

        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService(Mockito.anyBoolean());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(viewResults).when(notificationService).getAllCommonNotificationViewResults(Mockito.any(), Mockito.any(), Mockito.any());
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadMissingRestConnection() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.empty()).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());
    }

    @Test
    public void testReadException() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.doReturn(Optional.of(restConnection)).when(mockedBlackDuckProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doThrow(RuntimeException.class).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any());

        final BlackDuckAccumulator notificationAccumulator = createNonProcessingAccumulator(mockedBlackDuckProperties);
        final DateRange dateRange = notificationAccumulator.createDateRange(notificationAccumulator.getSearchRangeFilePath());
        final Optional<CommonNotificationViewResults> actualNotificationResults = notificationAccumulator.read(dateRange);
        assertFalse(actualNotificationResults.isPresent());

    }

    @Test
    public void testProcess() throws Exception {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties, processorList);
        final NotificationView notificationView = new NotificationView();
        notificationView.createdAt = new Date();
        notificationView.contentType = "content_type";
        notificationView.type = NotificationType.RULE_VIOLATION;
        notificationView.json = "{ content: \"content is here...\"}";
        final CommonNotificationView commonNotificationView = new CommonNotificationView(notificationView);
        final List<CommonNotificationView> viewList = Arrays.asList(commonNotificationView);
        final CommonNotificationViewResults notificationResults = new CommonNotificationViewResults(viewList, Optional.of(new Date()), Optional.of(RestConnection.formatDate(new Date())));
        final List<NotificationContent> notificationContentList = notificationAccumulator.process(notificationResults);
        assertFalse(notificationContentList.isEmpty());
    }

    @Test
    public void testProcessEmptyList() {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final BlackDuckAccumulator notificationAccumulator = createAccumulator(mockedBlackDuckProperties, processorList);
        final CommonNotificationViewResults viewList = new CommonNotificationViewResults(Collections.emptyList(), Optional.empty(), Optional.empty());
        final List<NotificationContent> contentList = notificationAccumulator.process(viewList);
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void testWrite() {
        final BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final BlackDuckAccumulator notificationAccumulator = new BlackDuckAccumulator(taskScheduler, testAlertProperties, testBlackDuckProperties, contentConverter, notificationManager, Collections.emptyList());

        final NotificationContent content = new MockNotificationContent(new Date(), "BlackDuck", "NotificationType", "{content: \"content is here\"}", null).createEntity();
        final List<NotificationContent> notificationContentList = Arrays.asList(content);
        notificationAccumulator.write(notificationContentList);

        Mockito.verify(notificationManager, Mockito.times(notificationContentList.size())).saveNotification(Mockito.any());
    }
}