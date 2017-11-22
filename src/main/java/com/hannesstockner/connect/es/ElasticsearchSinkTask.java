package com.hannesstockner.connect.es;

import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class ElasticsearchSinkTask extends SinkTask {

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchSinkTask.class);

  private String indexPrefix;
  private final String TYPE = "kafka";

  private RestHighLevelClient client;

  @Override
  public void start(Map<String, String> props) {
    final String esHost = props.get(ElasticsearchSinkConnector.ES_HOST);
    indexPrefix = props.get(ElasticsearchSinkConnector.INDEX_PREFIX);
    try {

      client = new RestHighLevelClient( RestClient.builder(
              new HttpHost(esHost, 9200, "http")));

    } catch (Exception ex) {
      throw new ConnectException("Couldn't connect to es host", ex);
    }
  }

  @Override
  public void put(Collection<SinkRecord> records) {
    for (SinkRecord record : records) {
      log.info("Processing {}", record.value());

      log.info(record.value().getClass().toString());
      IndexRequest request = new IndexRequest(indexPrefix + record.topic(), TYPE);
      request.source(record.value().toString(), XContentType.JSON);

      try {
        client.index(request);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
  }

  @Override
  public void stop() {
    try {
      client.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String version() {
    return new ElasticsearchSinkConnector().version();
  }
}
