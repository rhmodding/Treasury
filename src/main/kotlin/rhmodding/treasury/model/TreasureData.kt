package rhmodding.treasury.model

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class TreasureData(file: File, courses: List<TreasureCourse>) {
    val worlds = mutableListOf<TreasureWorld>()
    init {
        val b = Files.readAllBytes(Paths.get(file.absolutePath))
        val buf = ByteBuffer.wrap(b)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.position(0)
        while (buf.hasRemaining()) {
            val world = TreasureWorld()
            val amount = buf.get()
            for (i in 0 until amount) {
                world.courses.add(courses[buf.short.toInt()])
            }
            worlds.add(world)
        }
    }

    fun toBytes(): List<Byte> {
        return worlds.map { it.toBytes() }.flatten()
    }
}