package rhmodding.treasury

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import rhmodding.treasury.util.JsonHandler
import java.io.File

class Settings(val app: Treasury) {

    val prefsFolder: File = Treasury.rootFolder.resolve("prefs/").apply {
        mkdirs()
    }
    val prefsFile: File = prefsFolder.resolve("prefs.json")

    val nightModeProperty = SimpleBooleanProperty(false)
    var nightMode: Boolean
        get() = nightModeProperty.value
        set(value) = nightModeProperty.set(value)
    val defaultZlibDirectory: String = File(System.getProperty("user.home")).absolutePath
    val openZlibDirectory = SimpleStringProperty(defaultZlibDirectory)
    val saveZlibDirectory = SimpleStringProperty(defaultZlibDirectory)

    fun loadFromStorage() {
        if (!prefsFile.exists()) {
            return
        }
        try {
            val obj = JsonHandler.OBJECT_MAPPER.readTree(prefsFile)

            nightMode = obj["nightMode"]?.asBoolean(false) ?: false
            openZlibDirectory.set(obj["openZlibDirectory"]?.asText() ?: defaultZlibDirectory)
            saveZlibDirectory.set(obj["saveZlibDirectory"]?.asText() ?: defaultZlibDirectory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun persistToStorage() {
        prefsFile.createNewFile()
        val json = JsonHandler.OBJECT_MAPPER.createObjectNode()

        json.put("nightMode", nightMode)
        json.put("openZlibDirectory", openZlibDirectory.value)
        json.put("saveZlibDirectory", saveZlibDirectory.value)

        prefsFile.writeText(JsonHandler.OBJECT_MAPPER.writeValueAsString(json))
    }

}