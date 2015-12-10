# flume ng log4j sink
-------------------------

avro.conf

````
avroAgent.sources = avro
avroAgent.channels = memoryChannel fileCh
avroAgent.sinks = elasticSearch log4jSink

# For each one of the sources, the type is defined
avroAgent.sources.avro.type = avro
avroAgent.sources.avro.bind = 0.0.0.0
avroAgent.sources.avro.port = 1234 
avroAgent.sources.avro.threads=5
avroAgent.sources.avro.channels = memoryChannel fileCh 

# Each sink's type must be defined
avroAgent.sinks.elasticSearch.type = org.apache.flume.sink.elasticsearch.ElasticSearchSink

#Specify the channel the sink should use
avroAgent.sinks.elasticSearch.channel = memoryChannel
avroAgent.sinks.elasticSearch.batchSize = 100
avroAgent.sinks.elasticSearch.hostNames=172.16.0.18:9300 
avroAgent.sinks.elasticSearch.indexName=longdai
avroAgent.sinks.elasticSearch.indexType=longdai
avroAgent.sinks.elasticSearch.clusterName=longdai 
avroAgent.sinks.elasticSearch.client = transport
avroAgent.sinks.elasticSearch.serializer=org.apache.flume.sink.elasticsearch.ElasticSearchLogStashEventSerializer

avroAgent.sinks.log4jSink.type = org.apache.flume.sink.log4j.Log4jSink
avroAgent.sinks.log4jSink.channel = fileCh

avroAgent.channels.fileCh.type = file 

# Each channel's type is defined.
avroAgent.channels.memoryChannel.type = memory

# Other config values specific to each type of channel(sink or source)
# can be defined as well
# In this case, it specifies the capacity of the memory channel
avroAgent.channels.memoryChannel.capacity = 100

````




