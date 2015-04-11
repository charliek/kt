package com.charlieknudsen.kt;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KtConsumer implements Runnable {
	private final static Logger log = LoggerFactory.getLogger(KtConsumer.class);

	private final KafkaStream stream;
	private final BaseListenerBuilder listenerBuilder;

	public KtConsumer(KafkaStream stream, BaseListenerBuilder listenerBuilder) {
		this.stream = stream;
		this.listenerBuilder = listenerBuilder;
	}

	public void run() {
		ConsumerIterator<byte[], byte[]> it = stream.iterator();
		while (it.hasNext()) {
			byte[] bytes = it.next().message();
			for (BaseListener listener : listenerBuilder.getListeners()) {
				try {
					listener.onMessage(bytes);
				} catch (Exception e) {
					log.warn("Exception in listener script", e);
				}
			}
		}
		log.info("Shutting down listening thread");
	}
}