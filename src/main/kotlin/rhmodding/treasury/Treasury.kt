package rhmodding.treasury

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage
import rhmodding.treasury.util.ExceptionAlert
import rhmodding.treasury.util.TreasuryIcon
import rhmodding.treasury.util.setMinimumBoundsToSized
import java.io.File
import java.util.*
import kotlin.system.exitProcess


class Treasury : Application() {
    companion object {
        val VERSION: Version = Version(2, 0, 0, "DEVELOPMENT")
        const val GITHUB: String = """https://github.com/rhmodding/Treasury"""
        val rootFolder: File = File(System.getProperty("user.home")).resolve(".rhmodding/treasury/").apply { mkdirs() }
        val windowIcons: List<Image> by lazy { listOf(TreasuryIcon.icon32, TreasuryIcon.icon64) }
    }
    
    lateinit var primaryStage: Stage
        private set

    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage
        primaryStage.title = "Treasury $VERSION"
        primaryStage.icons.addAll(windowIcons)

        val scene = Scene(MainPane(this), 1000.0, 720.0)
        primaryStage.scene = scene
        primaryStage.setMinimumBoundsToSized()
        primaryStage.show()

        Thread.currentThread().setUncaughtExceptionHandler { t, e ->
            e.printStackTrace()
            Platform.runLater {
                val exitButton = ButtonType("Exit Program")
                val buttonType: Optional<ButtonType> = ExceptionAlert(null, e, "An uncaught exception occurred in thread ${t.name}\n${e::class.java.simpleName}", "An uncaught exception occurred").apply {
                    this.buttonTypes += exitButton
                }.showAndWait()
                if (buttonType.isPresent) {
                    if (buttonType.get() == exitButton) {
                        exitProcess(0)
                    }
                }
            }
        }
    }
    
}