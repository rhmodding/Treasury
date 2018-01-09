package rhmodding.treasury.model

import kotlin.experimental.and

class TreasureWorld {
	val courses = mutableListOf<TreasureCourse>()

	fun toBytes(): List<Byte> {
		val l = mutableListOf<Byte>()
		l.add(courses.size.toByte())
		for (c in courses) {
			l.add((c.id and 0xFF).toByte())
			l.add((c.id.toInt() ushr 8).toByte())
		}
		return l
	}
}