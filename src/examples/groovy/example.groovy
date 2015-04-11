List<String> getLogStrings() {
	[
			'ERROR',
			'WARN',
			'Exception',
	]
}

boolean isInteresting(String str) {
	for (i in logStrings) {
		if (str.contains(i)) {
			return true
		}
	}
	return false
}

void init() {
	// nothing to do yet
}

void onMessage(byte[] bytes) {
	def msg = new String(bytes, 'UTF-8')
	if (isInteresting(msg)) {
		println(msg)
	}
}
