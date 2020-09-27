package rhmodding.treasury

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
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
import javafx.stage.Stage

class MainPane(val app: Treasury) : BorderPane() {

    val toolbar: MenuBar = MenuBar()
    val centrePane: StackPane = StackPane()
    val bottomPane: VBox = VBox()
    
    init {
        top = toolbar
        center = centrePane
        bottom = bottomPane

        toolbar.menus += Menu("File").apply {
            this.items += MenuItem("Open extracted ZLIB directory").apply { 
                accelerator = KeyCombination.keyCombination("Shortcut+O")
            }
        }
        
        toolbar.menus += Menu("About").apply {
            this.items += MenuItem("About this program").apply {
                this.onAction = EventHandler {
                    val alert = Alert(Alert.AlertType.INFORMATION)
                    alert.title = "About Treasury"
                    alert.headerText = alert.title
                    (alert.dialogPane.scene.window as? Stage?)?.icons?.setAll(Treasury.windowIcons)
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
                    alert.showAndWait()
                }
            }
        }
        
        centrePane.children += Label("Open the directory with the extracted contents of treasure_world_data.zlib using\nFile > Open extracted ZLIB directory...").apply {
            id = "no-tabs-label"
            style = "-fx-font-size: 150%;"
            textAlignment = TextAlignment.CENTER
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            alignment = Pos.CENTER
        }
    }
    
}