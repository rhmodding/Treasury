package rhmodding.treasury

import javafx.beans.property.SimpleStringProperty
import rhmodding.treasury.util.JsonHandler
import java.io.File

class Settings(val app: Treasury) {

    val prefsFolder: File = Treasury.rootFolder.resolve("prefs/").apply {
        mkdirs()
    }
    val prefsFile: File = prefsFolder.resolve("prefs.json")

    val defaultZlibDirectory: String = File(System.getProperty("user.home")).resolve("Desktop/").absolutePath
    val zlibDirectory = SimpleStringProperty(defaultZlibDirectory)

    fun loadFromStorage() {
        if (!prefsFile.exists()) {
            return
        }
        try {
            val obj = JsonHandler.OBJECT_MAPPER.readTree(prefsFile)

            zlibDirectory.set(obj["zlibDirectory"]?.asText() ?: defaultZlibDirectory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun persistToStorage() {
        prefsFile.createNewFile()
        val json = JsonHandler.OBJECT_MAPPER.createObjectNode()

        json.put("zlibDirectory", zlibDirectory.value)

        prefsFile.writeText(JsonHandler.OBJECT_MAPPER.writeValueAsString(json))
    }

}