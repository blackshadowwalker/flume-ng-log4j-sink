package org.apache.flume.sink.log4j;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.util.Map;

/**
 * Created by karl on 2015/12/9.
 */
public class Log4jSink extends AbstractSink implements Configurable {
    private static final Logger log = Logger.getLogger(Log4jSink.class);

    public static final String LOG4J_CONFIG_FILE = "configFile";
    public static final String IGNORE_NOT_LOG4J_LOG = "ignoreNotLog4jLog";
    private boolean ignoreNotLog4jLog = true;
    public static final String DEFAULT_LOG4J_CONFIG_FILE = "log4j.properties";

    @Override
    public void configure(Context context) {
        ignoreNotLog4jLog = context.getBoolean(IGNORE_NOT_LOG4J_LOG, true);
        String log4jConfigFile = context.getString(LOG4J_CONFIG_FILE);
        log.info("log4jConfigFile:" + log4jConfigFile);
        if (log4jConfigFile != null) {
            File file = new File(log4jConfigFile);
            if (!file.exists()) {
                file = new File(DEFAULT_LOG4J_CONFIG_FILE);
            }
            if (file.exists()) {
                if (log4jConfigFile.endsWith(".xml")) {
                    DOMConfigurator.configureAndWatch(log4jConfigFile);
                } else {
                    PropertyConfigurator.configureAndWatch(log4jConfigFile);
                }
            }
        }
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status result = Status.READY;
        Channel channel = getChannel();
        Transaction transaction = channel.getTransaction();
        Event event = null;

        try {
            transaction.begin();
            event = channel.take();
            if (event != null) {
                Map<String, String> header = event.getHeaders();
                String loggerName = header.get(Log4jAvroHeaders.LOGGER_NAME.getName());
                String loggerLevel = header.get(Log4jAvroHeaders.LOG_LEVEL.getName());
                String timestamp = header.get(Log4jAvroHeaders.TIMESTAMP.getName());
                String encoding = header.get(Log4jAvroHeaders.MESSAGE_ENCODING.getName());
                if (loggerName == null || loggerLevel == null || timestamp == null) {
                    if (ignoreNotLog4jLog) {
                        transaction.commit();
                        return result;
                    } else {
                        log.info(new String(event.getBody()));
                        transaction.commit();
                        return result;
                    }
                }
                String msg = new String(event.getBody());
                Level level = Level.toLevel(Integer.parseInt(loggerLevel));
                Logger logger = Logger.getRootLogger().getLoggerRepository().getLogger(loggerName);
                if (logger == null) {
                    logger = Logger.getRootLogger();
                }
                LoggingEvent loggingEvent = new LoggingEvent(this.getClass().getName(), logger, Long.parseLong(timestamp), level, msg, null);
                Logger.getRootLogger().callAppenders(loggingEvent);
            } else {
                result = Status.BACKOFF;
            }
            transaction.commit();
        } catch (Exception ex) {
            log.error("", ex);
            if (ignoreNotLog4jLog) {
                transaction.commit();
            }else {
                transaction.rollback();
                throw new EventDeliveryException("Failed to log event: " + event, ex);
            }
        } finally {
            transaction.close();
        }
        return result;
    }

}
