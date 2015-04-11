package com.charlieknudsen.kt;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class Kt {

	private final static Logger log = LoggerFactory.getLogger(Kt.class);
	private Config config;
	private BaseListenerBuilder baseListenerBuilder;
	KtGroup consumerGroup;

	private void shutdownKafka() throws Exception {
		// May consider sleeping for a couple seconds here if we want to
		// make sure the zookeeper pointer is saved after exit.
		consumerGroup.shutdown();
	}

	private void tailKafka() throws Exception {
		consumerGroup = new KtGroup(config);
		consumerGroup.run(config.getNumThreads(), baseListenerBuilder);
	}

	private boolean parseCli(String[] args) {
		config = new Config();
		JCommander commander = new JCommander(config);
		try {
			commander.parse(args);
			List<String> errors = config.isValid();
			if (!errors.isEmpty()) {
				System.out.println("Errors detected:");
				errors.forEach(System.out::println);
				System.out.println("\n");
				commander.usage();
				return false;
			}
			return true;
		} catch (ParameterException e) {
			System.err.println(e.getMessage() + "\n");
			commander.usage();
			return false;
		}
	}

	private void blockForShutdown(Callable<Void> onShutdown) throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.warn("Beginning to shutdown kafka");
				try {
					onShutdown.call();
					log.warn("Shutdown complete.");
				} catch (Exception e) {
					log.error("Error caught during shutdown", e);
				}
				latch.countDown();
			}
		});
		latch.await();
	}

	private void run(String[] args) throws Exception {
		boolean cliOk = parseCli(args);
		if (!cliOk) {
			System.exit(1);
		}
		baseListenerBuilder = new BaseListenerBuilder(config.getScripts());
		baseListenerBuilder.start(Optional.empty());
		tailKafka();
		blockForShutdown(() -> {
			shutdownKafka();
			baseListenerBuilder.stop();
			return null;
		});
		log.info("Shutdown complete");
	}

	public static void main(String[] args) throws Exception {
		new Kt().run(args);
	}
}
