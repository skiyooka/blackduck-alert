package com.blackducksoftware.integration.alert.channel.hipchat.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatRepository;
import com.blackducksoftware.integration.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:spring-test.properties")
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalHipChatRepositoryIT {
    @Autowired
    private GlobalHipChatRepository repository;

    @Test
    public void testSaveEntity() {
        // make sure all the test data is deleted
        repository.deleteAll();
        final String apiKey = "api_key";
        final GlobalHipChatConfigEntity entity = new GlobalHipChatConfigEntity(apiKey, "");
        final GlobalHipChatConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final GlobalHipChatConfigEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(apiKey, foundEntity.getApiKey());
    }
}