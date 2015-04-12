package com.charlieknudsen.kt.color;

public class MatchObject {
	private final static MatchObject NOT_FOUND = new MatchObject(false, null);
	private final boolean found;
	private final String matching;

	public static MatchObject found(String matching) {
		return new MatchObject(true, matching);
	}

	public static MatchObject notFound() {
		return NOT_FOUND;
	}

	public MatchObject(boolean found, String matching) {
		this.found = found;
		this.matching = matching;
	}

	public String stringValue() {
		if (found) {
			return matching;
		} else {
			return null;
		}
	}

	/**
	 * Print the colorized string if there was a match
	 */
	public boolean print() {
		if (found) {
			System.out.println(matching);
		}
		return found;
	}

	public boolean found() {
		return found;
	}
}