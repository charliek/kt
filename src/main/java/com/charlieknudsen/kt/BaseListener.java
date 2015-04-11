package com.charlieknudsen.kt;

import groovy.lang.Script;

public abstract class BaseListener extends Script {

	/**
	 * init will be called once when your script is first
	 * initialized. Useful for doing setup work.
	 */
	public void init() {
		// Nothing done by default. Override if you have setup logic.
	}

	public void onMessage(byte[] bytes) {
		// You should override this.
	}
}
