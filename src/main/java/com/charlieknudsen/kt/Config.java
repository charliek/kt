package com.charlieknudsen.kt;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
	static private final List<String> VALID_LOCATIONS = Arrays.asList("head", "tail");

	@Parameter(names = {"-s", "-script"}, required = true, description = "The scripts you would like to load. " +
			"Can be specified multiple times.")
	private List<String> scripts = new ArrayList<String>();

	@Parameter(names = "-zk", required = true, description = "The zookeeper host(s) hostname1:port1/chroot/path.")
	private String zookeeper;

	@Parameter(names = "-topic", required = true, description = "The kafka topic name to listen to.")
	private String topic;

	@Parameter(names = "-threads", required = false, description = "The number of kafka worker threads.")
	private int numThreads = 1;

	@Parameter(names = "-loc", required = false, description = "Start from head or tail of topic (use 'head' to override).")
	private String location = "tail";

	@Parameter(names = "-gid", required = false, description = "Specify a non-random group id if you need to pick" +
			" up where you left off.")
	private String groupId = null;

	public int getNumThreads() {
		return numThreads;
	}

	public List<String> getScripts() {
		return scripts;
	}

	public String getZookeeper() {
		return zookeeper;
	}

	public String getTopic() {
		return topic;
	}

	public String getLocation() {
		return location;
	}

	public String getGroupId() {
		return groupId;
	}

	/**
	 * Validation that is required beyond the jcommander parsing
	 */
	public List<String> isValid() {
		List<String> errors = new ArrayList<>();
		if (! VALID_LOCATIONS.contains(location)) {
			errors.add(location + " is not a valid location. Please choose 'head' or 'tail'.");
		}
		return errors;
	}
}
