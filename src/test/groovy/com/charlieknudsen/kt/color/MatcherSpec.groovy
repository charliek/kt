package com.charlieknudsen.kt.color

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class MatcherSpec extends Specification {

	@Shared
	String color = Color.GREEN.escape

	@Unroll("Matching text #toMatch")
	def 'text matching'() {
		given:
		Matcher c = new Matcher(['abc', 'def'])

		when:
		MatchObject m = c.search(toMatch)

		then:
		m.found()
		m.stringValue() == expected.toString()

		where:
		toMatch          | expected
		"abdefbbhdefkdk" | "ab${color}def${Matcher.ANSI_RESET}bbh${color}def${Matcher.ANSI_RESET}kdk"
		"abcxxxxxxxabc"  | "${color}abc${Matcher.ANSI_RESET}xxxxxxx${color}abc${Matcher.ANSI_RESET}"
		"xxxabcxxxdefx"  | "xxx${color}abc${Matcher.ANSI_RESET}xxxdefx"
		"def"            | "${color}def${Matcher.ANSI_RESET}"
		"abcx"           | "${color}abc${Matcher.ANSI_RESET}x"
		"xabc"           | "x${color}abc${Matcher.ANSI_RESET}"
	}

	def 'non matches are not found'() {
		given:
		Matcher c = new Matcher(['abc'])

		when:
		MatchObject m = c.search("blah")

		then:
		!m.found()
	}

	def 'no color does not change string'() {
		given:
		String s = 'xijkx'
		Matcher c = new Matcher(['ijk'], Color.NONE)

		when:
		MatchObject m = c.search(s)

		then:
		m.found()
		m.stringValue() == s
	}
}
