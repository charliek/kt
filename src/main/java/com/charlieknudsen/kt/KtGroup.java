package com.charlieknudsen.kt;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.consumer.KafkaStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KtGroup {
	private final static Logger log = LoggerFactory.getLogger(KtGroup.class);
	private final ConsumerConnector consumer;
	private final String topic;
	private ExecutorService executor;

	public KtGroup(Config config) {
		// Because we are not pushing names to zookeeper random names should be fine
		String groupId = config.getGroupId();
		if (groupId == null) {
			// default to a unique group id
			groupId = "Kt-" + UUID.randomUUID();
		}

		String offset = "largest";
		if (config.getLocation().equals("tail")) {
			offset = "smallest";
		}
		log.info("Starting consumer at '{}' offset", offset);
		consumer = Consumer.createJavaConsumerConnector(createConsumerConfig(config.getZookeeper(), groupId, offset));
		this.topic = config.getTopic();
	}

	public void shutdown() {
		if (consumer != null) consumer.shutdown();
		if (executor != null) executor.shutdown();
	}

	public void run(int numThreads, BaseListenerBuilder listenerBuilder) {
		Map<String, Integer> topicCountMap = new HashMap<>();
		topicCountMap.put(topic, numThreads);
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

		executor = Executors.newFixedThreadPool(numThreads);
		streams.forEach(stream -> executor.submit(new KtConsumer(stream, listenerBuilder)));
	}

	private static ConsumerConfig createConsumerConfig(String zookeeper, String groupId, String offset) {
		// http://kafka.apache.org/08/configuration.html
		Properties props = new Properties();
		props.put("zookeeper.connect", zookeeper);
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("group.id", groupId);

		// Turn off managing the offset in zookeeper and always start at the tail
		// if we enable this in the future make sure to set 'auto.commit.interval.ms'
		props.put("auto.commit.enable", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.offset.reset", offset);

		return new ConsumerConfig(props);
	}
}

