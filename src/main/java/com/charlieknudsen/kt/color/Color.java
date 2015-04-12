package com.charlieknudsen.kt.color;

public enum Color {
	GREEN("\u001B[32m"),
	BLUE("\u001B[34m"),
	YELLOW("\u001B[33m"),
	RED("\u001B[31m"),
	PURPLE("\u001B[35m"),
	CYAN("\u001B[36m"),
	BLACK("\u001B[30m"),
	WHITE("\u001B[37m"),
	NONE("");

	private final String escape;

	Color(String escape) {
		this.escape = escape;
	}

	public String getEscape() {
		return escape;
	}
}