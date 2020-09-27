package rhmodding.treasury.model

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
        return """${
            if (games.size == 1) {
                "${games[0].id} (${TreasureGame.GAMENAMES[games[0].id.toInt()]})"
            } else if (random) "Random" else "Multiple Choice"
        }
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
        val GAMENAMES: List<String> = listOf("Clappy Trio", "Sneaky Spirits", "Rhythm Tweezers", "Glee Club", "Rhythm Rally", "Fillbots", "Shoot-'em-Up", "Air Rally", "Micro-Row", "Flipper-Flop", "Figure Fighter", "Fruit Basket", "First Contact", "Catchy Tune", "LumBEARjack", "Spaceball", "Clappy Trio 2", "Sneaky Spirits 2", "Rhythm Tweezers 2", "Bouncy Road", "Marching Orders", "Night Walk", "Quiz Show", "Bunny Hop", "Rat Race", "Power Calligraphy", "Space Dance", "Tap Trial", "Ninja Bodyguard", "Airboarder", "Lockstep", "Blue Birds", "Dazzles", "Freeze Frame", "Glee Club 2", "Frog Hop", "Fan Club", "Dog Ninja", "Rhythm Rally 2", "Fillbots 2", "Shoot-'em-Up 2", "Big Rock Finish", "Munchy Monk", "Built to Scale", "Air Rally 2", "Exhibition Match", "Flockstep", "Cheer Readers", "Double Date", "Catch of the Day", "Micro-Row 2", "Fork Lifter", "Hole in One", "Flipper-Flop 2", "Ringside", "Working Dough", "Figure Fighter", "Love Rap", "Bossa Nova", "Screwbots", "Launch Party", "Board Meeting", "Samurai Slice", "See-Saw", "Packing Pests", "Monkey Watch", "Blue Bear", "Animal Acrobat", "Tongue Lashing", "Super Samurai Slice", "Fruit Basket 2", "Second Contact", "Pajama Party", "Catchy Tune 2", "Sumo Brothers", "Tangotronic 3000", "Kitties!", "LumBEARjack 2", "Snappy Trio", "Cosmic Dance", "Tap Trial 2", "Jumpin' Jazz", "Fan Club 2", "Cosmic Rhythm Rally", "Hole in One 2", "Working Dough 2", "Figure Fighter 3", "Jungle Gymnast", "Super Samurai Slice 2", "Karate Man", "Karate Man Returns!", "Karate Man Kicks!", "Karate Man Combos!", "Karate Man Senior", "Lush Remix", "Final Remix", "Honeybee Remix", "Machine Remix", "Citrus Remix", "Donut Remix", "Barbershop Remix", "Songbird Remix", "Left-Hand Remix", "Right-Hand Remix")
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