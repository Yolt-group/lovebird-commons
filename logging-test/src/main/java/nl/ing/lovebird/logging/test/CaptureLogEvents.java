package nl.ing.lovebird.logging.test;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.LoggerFactory;

/**
 * See CaptureLogEventsDemoTest for usage.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(CaptureLogEvents.Extension.class)
public @interface CaptureLogEvents {

    class Extension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

        @Override
        public void beforeEach(ExtensionContext context) {
            Logger logger = getRootLogger();
            logger.addAppender(getListener(context));
        }


        @Override
        public void afterEach(ExtensionContext context) {
            Logger logger = getRootLogger();
            logger.detachAppender(getListener(context));
        }

        private Logger getRootLogger() {
            return (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            boolean isTestMethodLevel = extensionContext.getTestMethod().isPresent();
            boolean isListener = parameterContext.getParameter().getType() == LogEvents.class;
            return isTestMethodLevel && isListener;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return getListener(extensionContext);
        }

        private Appender<ILoggingEvent> getListener(ExtensionContext context) {
            return getStore(context).getOrComputeIfAbsent(LogEvents.class);
        }

        private Store getStore(ExtensionContext context) {
            return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
        }

    }

}
