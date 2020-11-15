package rhmodding.treasury

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.scene.text.TextFlow
import javafx.stage.DirectoryChooser
import rhmodding.treasury.model.TreasureCourse
import rhmodding.treasury.model.TreasureData
import java.io.File

class MainPane(val app: Treasury) : BorderPane() {

    val toolbar: MenuBar = MenuBar()
    val centrePane: StackPane = StackPane()
    val bottomPane: VBox = VBox()
    
    val editor: SimpleObjectProperty<EditorPane?> = SimpleObjectProperty(null)
    
    init {
        stylesheets += "style/mainPane.css"
        
        top = toolbar
        center = centrePane
        bottom = bottomPane
        
        fun File.ensureDirectory(): File =
            if (!this.exists() || !this.isDirectory)
                File(app.settings.defaultZlibDirectory)
            else this

        toolbar.menus += Menu("File").apply {
            this.items += MenuItem("Open extracted ZLIB directory").apply { 
                accelerator = KeyCombination.keyCombination("Shortcut+O")
                setOnAction {
                    val dirChooser = DirectoryChooser()
                    dirChooser.title = "Choose a directory containing the contents of treasure_world_data.zlib"
                    dirChooser.initialDirectory = File(app.settings.openZlibDirectory.get()).ensureDirectory()

                    val f = dirChooser.showDialog(null)
                    if (f != null) {
                        val path = f.parent
                        val courses = (f.listFiles { _, name -> name.startsWith("world_data_") } ?: arrayOf())
                                .map { TreasureCourse(it) }
                                .sortedBy { it.id }
                        val l = f.listFiles { _, name -> name.startsWith("course_data.bin") }?.toList() ?: emptyList()
                        if (l.isNotEmpty()) {
                            val treasureData = TreasureData(l.first(), courses)
                            val last = editor.get()
                            if (last != null) {
                                centrePane.children.remove(last)
                            }
                            val newPane = EditorPane(app, treasureData)
                            editor.set(newPane)
                            centrePane.children.add(newPane)
                            app.settings.openZlibDirectory.set(path)
                        } else {
                            Alert(Alert.AlertType.ERROR).apply { 
                                title = "Invalid directory"
                                headerText = title
                                dialogPane.contentText = "No course_data.bin found in the selected directory"
                                this@MainPane.app.addBaseStyleToAlert(this)
                            }.showAndWait()
                        }
                    }
                }
            }
            this.items += MenuItem("Save changes to a directory").apply {
                accelerator = KeyCombination.keyCombination("Shortcut+S")
                disableProperty().bind(editor.isNull)
                setOnAction {
                    val treasureData: TreasureData? = editor.get()?.treasureData
                    if (treasureData != null) {
                        val dirChooser = DirectoryChooser()
                        dirChooser.title = "Choose a directory to save your changes in"
                        dirChooser.initialDirectory = File(app.settings.saveZlibDirectory.get()).ensureDirectory()

                        val f = dirChooser.showDialog(null)
                        if (f != null) {
                            val path = f.parent
                            val courseDataFile = File(f, "course_data.bin")
                            courseDataFile.writeBytes(treasureData.toBytes().toByteArray())
                            for (w in treasureData.worlds) {
                                for (c in w.courses) {
                                    val worldDataFile = File(f, "world_data_${c.id}.bin")
                                    worldDataFile.writeBytes(c.toBytes().toByteArray())
                                }
                            }
                            app.settings.saveZlibDirectory.set(path)
                        }
                    }
                }
            }
        }

        toolbar.menus += Menu("View").apply {
            items += CheckMenuItem("Dark Mode").apply {
                selectedProperty().bindBidirectional(app.settings.nightModeProperty)
            }
        }
        
        toolbar.menus += Menu("About").apply {
            items += MenuItem("Report a bug/problem").apply {
                setOnAction {
                    app.hostServices.showDocument("${Treasury.GITHUB}/issues/new")
                }
            }
            items += MenuItem("Check for updates").apply {
                setOnAction {
                    app.hostServices.showDocument("${Treasury.GITHUB}/releases")
                }
            }
            items += SeparatorMenuItem()
            this.items += MenuItem("About this program").apply {
                this.onAction = EventHandler {
                    val alert = Alert(Alert.AlertType.INFORMATION)
                    alert.title = "About Treasury"
                    alert.headerText = alert.title
                    alert.dialogPane.content = TextFlow(
                            Text("Treasury\n${Treasury.VERSION}\n"),
                            Text("\nLicensed under the"),
                            Hyperlink("MIT license").apply {
                                setOnAction {
                                    HostServicesFactory.getInstance(app).showDocument("https://github.com/rhmodding/Treasury/blob/master/LICENSE")
                                }
                            },
                            Text("\n"),
                            Hyperlink(Treasury.GITHUB).apply {
                                setOnAction {
                                    HostServicesFactory.getInstance(app).showDocument(Treasury.GITHUB)
                                }
                            }
                    )
                    app.addBaseStyleToAlert(alert)
                    alert.showAndWait()
                }
            }
        }
        
        centrePane.children += Label("Open the directory with the extracted contents of treasure_world_data.zlib using\nFile > Open extracted ZLIB directory...").apply {
            id = "no-tabs-label"
            textAlignment = TextAlignment.CENTER
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            alignment = Pos.CENTER
            visibleProperty().bind(editor.isNull)
        }
    }
    
}