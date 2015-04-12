import com.charlieknudsen.kt.color.Color
import com.charlieknudsen.kt.color.Matcher

def matcher

void init() {
	matcher = new Matcher([
		'ERROR',
		'WARN',
		'Exception',
	], Color.CYAN)
}

void onMessage(byte[] bytes) {
	def msg = new String(bytes, 'UTF-8')
	matcher.search(msg).print()
}
