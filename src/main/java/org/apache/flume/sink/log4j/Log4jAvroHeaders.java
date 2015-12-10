package org.apache.flume.sink.log4j;

import java.util.Locale;

/**
 * Created by karl on 2015/12/9.
 */
public enum Log4jAvroHeaders {

    OTHER("flume.client.log4j.logger.other"),
    LOGGER_NAME("flume.client.log4j.logger.name"),
    LOG_LEVEL("flume.client.log4j.log.level"),
    MESSAGE_ENCODING("flume.client.log4j.message.encoding"),
    TIMESTAMP("flume.client.log4j.timestamp"),
    AVRO_SCHEMA_LITERAL("flume.avro.schema.literal"),
    AVRO_SCHEMA_URL("flume.avro.schema.url");

    private String headerName;
    private Log4jAvroHeaders(String headerName){
        this.headerName = headerName;
    }

    public String getName(){
        return headerName;
    }

    public String toString(){
        return getName();
    }

    public static Log4jAvroHeaders getByName(String headerName){
        Log4jAvroHeaders hdrs = null;
        try{
            hdrs = Log4jAvroHeaders.valueOf(headerName.toLowerCase(Locale.ENGLISH).trim());
        }
        catch(IllegalArgumentException e){
            hdrs = Log4jAvroHeaders.OTHER;
        }
        return hdrs;
    }

}
