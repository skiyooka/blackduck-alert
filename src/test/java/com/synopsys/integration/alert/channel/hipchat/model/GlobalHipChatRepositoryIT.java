package com.synopsys.integration.alert.channel.hipchat.model;

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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:spring-test.properties")
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalHipChatRepositoryIT {
    @Autowired
    private HipChatGlobalRepository repository;

    @Test
    public void testSaveEntity() {
        // make sure all the test data is deleted
        repository.deleteAll();
        final String apiKey = "api_key";
        final HipChatGlobalConfigEntity entity = new HipChatGlobalConfigEntity(apiKey, "");
        final HipChatGlobalConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final HipChatGlobalConfigEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(apiKey, foundEntity.getApiKey());
    }
}