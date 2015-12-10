package org.apache.flume.sink.log4j;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Map;

/**
 * Created by karl on 2015/12/9.
 */
public class Log4jSink extends AbstractSink implements Configurable {
    private static final Logger log = Logger.getLogger(Log4jSink.class);

    @Override
    public void configure(Context context) {

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
                String loggerName = header.get(Log4jAvroHeaders.LOGGER_NAME);
                String loggerLevel = header.get(Log4jAvroHeaders.LOG_LEVEL);
                String timestamp = header.get(Log4jAvroHeaders.TIMESTAMP);
                String encoding = header.get(Log4jAvroHeaders.MESSAGE_ENCODING);
                String msg = new String(event.getBody());
                Level level = Level.toLevel(Integer.parseInt(loggerLevel));
                Logger logger = Logger.getRootLogger().getLoggerRepository().getLogger(loggerName);
                if(logger==null){
                    logger = Logger.getRootLogger();
                }
                LoggingEvent loggingEvent = new LoggingEvent(this.getClass().getName(), logger, Long.parseLong(timestamp), level, msg, null);
                Logger.getRootLogger().callAppenders(loggingEvent);
            } else {
                result = Status.BACKOFF;
            }
            transaction.commit();
        } catch (Exception ex) {
            transaction.rollback();
            log.error("", ex);
            throw new EventDeliveryException("Failed to log event: " + event, ex);
        } finally {
            transaction.close();
        }
        return result;
    }

}
