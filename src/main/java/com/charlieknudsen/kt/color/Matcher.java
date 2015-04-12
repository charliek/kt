package com.charlieknudsen.kt.color;

import java.util.List;
import java.util.Optional;

public class Matcher {
	public static final String ANSI_RESET = "\u001B[0m";
	private final List<String> matchers;
	private final Color color;

	public Matcher(List<String> matchers) {
		this(matchers, Color.GREEN);
	}

	public Matcher(List<String> matchers, Color color) {
		this.matchers = matchers;
		this.color = color;
	}

	public MatchObject search(String msg) {
		Optional<MatchObject> m = matchers.stream()
				.map(matcher -> search(msg, matcher))
				.filter(MatchObject::found)
				.findFirst();
		return m.orElse(MatchObject.notFound());
	}

	public MatchObject search(String msg, String match) {
		StringBuilder sb = new StringBuilder();
		boolean matchFound = false;
		int index = 0;
		while (true) {
			int loc = msg.indexOf(match, index);
			if (loc != -1) {
				matchFound = true;
				if (color == Color.NONE) {
					return MatchObject.found(msg);
				}
				sb.append(msg.substring(index, loc))
						.append(color.getEscape())
						.append(match)
						.append(ANSI_RESET);
				index = loc + match.length();
			} else {
				if (matchFound) {
					sb.append(msg.substring(index));
					return MatchObject.found(sb.toString());
				} else {
					return MatchObject.notFound();
				}
			}
		}
	}
}
