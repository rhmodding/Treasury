package rhmodding.treasury.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.experimental.and

class TreasureCourse(file: File) {
	
	companion object {
		val COURSENAMES: List<String> = Gson().fromJson(this::class.java.classLoader.getResourceAsStream("coursenames.json")?.readBytes()?.toString(Charsets.UTF_8), object : TypeToken<List<String>>() {}.type)
	}
	
	var id: Short
	var superHard: Boolean
	var flowBalls: Byte
	val coursesToUnlock = mutableListOf<Short>()
	var unk: Byte
	val groups = mutableListOf<TreasureGroup>()

	init {
		val b = Files.readAllBytes(Paths.get(file.absolutePath))
		val buf = ByteBuffer.wrap(b)
		buf.order(ByteOrder.LITTLE_ENDIAN)
		buf.position(0)
		id = buf.short
		superHard = buf.get() != 0.toByte()
		flowBalls = buf.get()
		val amount = buf.get()
		for (i in 0 until amount) {
			coursesToUnlock.add(buf.short)
		}
		unk = buf.get()
		val gameAmount = buf.get()
		for (i in 0 until gameAmount) {
			groups.add(TreasureGroup(buf))
		}
	}

	fun toBytes(): List<Byte> {
		val l = mutableListOf<Byte>()
		l.add((id and 0xFF).toByte())
		l.add((id.toInt() ushr 8).toByte())
		l.add(if (superHard) 1.toByte() else 0.toByte())
		l.add(flowBalls)
		l.add(coursesToUnlock.size.toByte())
		for (c in coursesToUnlock){
			l.add((c and 0xFF).toByte())
			l.add((c.toInt() ushr 8).toByte())
		}
		l.add(unk)
		l.add(groups.size.toByte())
		for (g in groups) {
			l.addAll(g.toBytes())
		}
		return l
	}

	override fun toString(): String {
		return """$id (${COURSENAMES[id.toInt()]})"""
	}
}

data class CourseNumber(var id: Int) {
	override fun toString(): String {
		return """$id (${TreasureCourse.COURSENAMES[id.toInt()]})"""
	}
}