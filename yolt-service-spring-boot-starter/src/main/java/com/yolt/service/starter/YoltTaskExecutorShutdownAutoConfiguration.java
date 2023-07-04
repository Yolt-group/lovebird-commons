package com.yolt.service.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;

import java.util.concurrent.Executor;

/**
 * Pods may perform Async tasks that require the creation of beans.
 * This poses a problem when the spring application is being shutdown with
 * tasks in the queue. Tasks will fail because they can not request beans from
 * the registry while the application is shutting down.
 * <p>
 * As such the queue should be cleared **before** the the application shuts
 * down.
 * <p>
 * While `setWaitForTasksToCompleteOnShutdown` does indeed prevent
 * application shutdown, because the `ThreadPoolTaskExecutor` is a
 * disposable bean, this happens while the application is shutting down.
 * <p>
 * By making the `ThreadPoolTaskExecutor` and `ThreadPoolTaskScheduler` lifecycle
 * aware we can shut down the thread pool prior to application shutdown.
 */
@AutoConfiguration
@RequiredArgsConstructor
@Slf4j
public class YoltTaskExecutorShutdownAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor makeExecutorsLifeCycleAware() {
        return new SmartLifecyclePostProcessor();
    }

    @RequiredArgsConstructor
    public static class SmartLifecyclePostProcessor implements BeanFactoryPostProcessor, SmartInitializingSingleton {

        private ConfigurableListableBeanFactory beanFactory;

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void afterSingletonsInstantiated() {
            // All beans have created. We can safely look up executors without risking
            // premature instantiation. See BeanFactoryPostProcessor
            beanFactory.getBeansOfType(Executor.class).forEach((beanName, executor) -> {
                if (executor instanceof ExecutorConfigurationSupport) {
                    ExecutorConfigurationSupport support = (ExecutorConfigurationSupport) executor;
                    String smartLifeCycleBean = beanName + "_smartLifecycle";
                    beanFactory.registerSingleton(smartLifeCycleBean, new SmartLifecycleAdaptor(beanName, support));
                    beanFactory.registerDependentBean(beanName, smartLifeCycleBean);
                }
            });
        }
    }

    @RequiredArgsConstructor
    public static class SmartLifecycleAdaptor implements SmartLifecycle {

        private final String delegateBeanName;
        private final ExecutorConfigurationSupport delegate;
        private boolean running = false;

        @Override
        public void start() {
            log.info("Starting via lifecycle: ExecutorService '" + this.delegateBeanName + "'");
            running = true;
        }

        @Override
        public void stop() {
            log.info("Stopping via lifecycle: ExecutorService '" + this.delegateBeanName + "'");
            delegate.shutdown();
            running = false;
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public int getPhase() {
            return Integer.MIN_VALUE; // Start first, shut down last
        }
    }
}
