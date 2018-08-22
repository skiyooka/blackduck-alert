package com.blackducksoftware.integration.alert.config;

import org.mockito.Mockito;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.alert.provider.hub.accumulator.HubAccumulatorProcessor;
import com.blackducksoftware.integration.alert.provider.hub.accumulator.HubAccumulatorReader;
import com.blackducksoftware.integration.alert.provider.hub.accumulator.HubAccumulatorWriter;

public class AccumulatorConfigTest extends CommonConfigTest<HubAccumulatorReader, HubAccumulatorWriter, HubAccumulatorProcessor, AccumulatorConfig> {

    @Override
    public String getJobName() {
        return "AccumulatorJob";
    }

    @Override
    public String getStepName() {
        return "AccumulatorStep";
    }

    @Override
    public AccumulatorConfig getConfigWithNullParams() {
        return new AccumulatorConfig(null, null, null, null, null, null, getGlobalProperties(), null, null, null, contentConverter);
    }

    @Override
    public AccumulatorConfig getConfigWithSimpleJobLauncher(final SimpleJobLauncher simpleJobLauncher) {
        return new AccumulatorConfig(simpleJobLauncher, null, null, null, null, null, getGlobalProperties(), null, null, null, contentConverter);
    }

    @Override
    public AccumulatorConfig getConfigWithTaskScheduler(final TaskScheduler taskScheduler) {
        return new AccumulatorConfig(null, null, null, null, null, null, getGlobalProperties(), taskScheduler, null, null, contentConverter);
    }

    @Override
    public AccumulatorConfig getConfigWithParams(final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final PlatformTransactionManager platformTransactionManager) {
        return new AccumulatorConfig(null, null, stepBuilderFactory, taskExecutor, null, platformTransactionManager, getGlobalProperties(), null, null, null, contentConverter);
    }

    @Override
    public HubAccumulatorReader getMockReader() {
        return Mockito.mock(HubAccumulatorReader.class);
    }

    @Override
    public HubAccumulatorProcessor getMockProcessor() {
        return Mockito.mock(HubAccumulatorProcessor.class);
    }

    @Override
    public HubAccumulatorWriter getMockWriter() {
        return Mockito.mock(HubAccumulatorWriter.class);
    }

}