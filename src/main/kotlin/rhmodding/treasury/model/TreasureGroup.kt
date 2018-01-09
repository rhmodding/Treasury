package rhmodding.treasury.model

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import java.io.File
import java.nio.ByteBuffer
import kotlin.experimental.and

class TreasureGroup(buf: ByteBuffer?) {
	var random: Boolean = false
	var choices: Byte = 1
	var goal: GoalType = GoalType.Points
	var goalArg: Short = 0
	var tempo: Byte = 100
	val games = mutableListOf<TreasureGame>()

	init {
		if (buf == null) {
			games.add(TreasureGame(0, 100))
		} else {
			random = buf.get() != 0.toByte()
			choices = buf.get()
			goal = GoalType.fromByte(buf.get())!!
			goalArg = buf.short
			tempo = buf.get()
			val gameAmount = buf.get()
			for (i in 0 until gameAmount) {
				games.add(TreasureGame(buf.short, buf.get()))
			}
		}
	}

	fun toBytes(): List<Byte> {
		choices = if (random) 1.toByte() else games.size.toByte()
		val l = mutableListOf<Byte>()
		l.add(if (random) 1.toByte() else 0.toByte())
		l.add(choices)
		l.add(goal.n)
		l.add((goalArg and 0xFF).toByte())
		l.add((goalArg.toInt() ushr 8).toByte())
		l.add(tempo)
		l.add(games.size.toByte())
		for (g in games) {
			l.add((g.id and 0xFF).toByte())
			l.add((g.id.toInt() ushr 8).toByte())
			l.add(g.unkPercentage)
		}
		return l
	}

	override fun toString(): String {
		return """${if (games.size == 1) {"${games[0].id} (${ TreasureGame.GAMENAMES[games[0].id.toInt()]})"} else if (random) "Random" else "Multiple Choice"}
	$goal, $goalArg
	${tempo.toInt() and 0xFF}% tempo"""
	}
}

enum class GoalType(val n: Byte) {
	Points(0),
	Lives(1),
	Monster(2);
	companion object {
		private val map = GoalType.values().associateBy(GoalType::n);
		fun fromByte(type: Byte) = map[type]
	}
}

data class TreasureGame(var id: Short, var unkPercentage: Byte) {
	companion object {
		val GAMENAMES: List<String> = Gson().fromJson(File("./gamenames.json").readText())
	}

	override fun toString(): String {
		return "$id (${GAMENAMES[id.toInt()]}); $unkPercentage%"
	}
}

data class GameNumber(var id: Short) {
	override fun toString(): String {
		return "$id (${TreasureGame.GAMENAMES[id.toInt()]})"
	}
}