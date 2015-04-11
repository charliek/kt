package com.charlieknudsen.kt;

import com.google.common.collect.ImmutableList;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class watches the file system for changes if polling is enabled
 */
public class BaseListenerBuilder {

	private final static Logger log = LoggerFactory.getLogger(BaseListenerBuilder.class);

	private final Set<Path> paths;
	private volatile ImmutableList<BaseListener> listeners;
	private volatile boolean stopped = false;
	private Thread watchThread;

	public BaseListenerBuilder(List<String> fileNames) {
		paths = addFiles(fileNames);
		listeners = buildClasses();
	}

	synchronized public void stop() {
		if (watchThread != null) {
			stopped = true;
			watchThread.interrupt();
		}
	}

	synchronized public void start(Optional<Consumer<ImmutableList<BaseListener>>> changeConsumer) {
		log.info("Beginning file watcher");
		if (watchThread != null) {
			throw new IllegalStateException("Start must only be called once");
		}
		watchThread = new Thread(() -> this.watchFiles(changeConsumer));
		watchThread.start();
	}

	public ImmutableList<BaseListener> getListeners() {
		return listeners;
	}

	// http://stackoverflow.com/questions/16251273/can-i-watch-for-single-file-change-with-watchservice-not-the-whole-directory
	// http://www.codejava.net/java-se/file-io/file-change-notification-example-with-watch-service-api
	private void watchFiles(Optional<Consumer<ImmutableList<BaseListener>>> changeConsumer) {
		Set<Path> dirs = paths.stream()
				.map(Path::getParent)
				.distinct()
				.collect(Collectors.toCollection(HashSet::new));

		try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
			for (Path dir : dirs) {
				dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			}
			while (true) {
				if (stopped) {
					return;
				}
				final WatchKey wk = watchService.take();
				for (WatchEvent<?> event : wk.pollEvents()) {
					//we only register "ENTRY_MODIFY" so the context is always a Path.
					final Path changed = (Path) event.context();
					if (paths.stream().anyMatch(p -> p.endsWith(changed))) {
						log.info("Rebuilding listener classes detected a change in file {}", changed);
						listeners = buildClasses();
						if (changeConsumer.isPresent()) {
							changeConsumer.get().accept(listeners);
						}
					}
				}
				if (! wk.reset()) {
					break;
				}
			}
		} catch (InterruptedException e) {
			// ignore and exit
		} catch (IOException e) {
			log.error("Error when trying to watch the file system", e);
		}
		log.warn("Exiting groovy script polling loop");
	}

	private Set<Path> addFiles(List<String> fileNames) {
		Set<Path> paths = new HashSet<>();
		FileSystem fs = FileSystems.getDefault();
		fileNames.forEach(file -> {
			if (!file.endsWith(".groovy")) {
				throw new IllegalArgumentException("Only groovy scripts are supported: " + file);
			}
			Path p = fs.getPath(file);
			if (Files.isRegularFile(p)) {
				paths.add(p.toAbsolutePath());
			} else {
				throw new IllegalArgumentException("Unable to find a file at location: " + file);
			}
		});
		return paths;
	}

	private ImmutableList<BaseListener> buildClasses() {
		List<BaseListener> listeners = new ArrayList<>();
		CompilerConfiguration compiler = new CompilerConfiguration();
		compiler.setScriptBaseClass(BaseListener.class.getName());
		GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader(), compiler);
		paths.forEach(path -> {
			try {
				final Class clazz = loader.parseClass(path.toFile());
				BaseListener listener = (BaseListener) clazz.newInstance();
				listener.init();
				listeners.add(listener);
			} catch (Exception e) {
				log.error("Error building or initializing groovy script " + path, e);
			}
		});
		return ImmutableList.copyOf(listeners);
	}
}
