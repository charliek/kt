statusListener(OnConsoleStatusListener)
scan("30 seconds")

appender("CONSOLE", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
	}
}
logger("com.charlieknudsen", INFO)
root(WARN, ["CONSOLE"])

