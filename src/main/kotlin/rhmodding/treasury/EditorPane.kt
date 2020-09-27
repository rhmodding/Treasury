package rhmodding.treasury

import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import rhmodding.treasury.model.*
import rhmodding.treasury.util.intSpinnerFactory


class EditorPane(val app: Treasury, val treasureData: TreasureData) : BorderPane() {

    val centreVbox: VBox = VBox()
    val worldSpinner: ComboBox<String> = ComboBox(FXCollections.observableArrayList("0 - Saffron World", "1 - Saltwater World", "2 - Paprika World"))
    val courses: ComboBox<TreasureCourse> = ComboBox(FXCollections.observableArrayList())
    val groups: ListView<TreasureGroup> = ListView(FXCollections.observableArrayList())
    val games: ListView<TreasureGame> = ListView(FXCollections.observableArrayList())
    
    val coursesSuperHard: CheckBox = CheckBox()
    val coursesFlowBalls: Spinner<Int> = intSpinnerFactory(0, 255, 1, 1)
    val coursesUnknownSpinner: Spinner<Int> = intSpinnerFactory(0, 255, 3, 1)
    val coursesPrereqs: ListView<CourseNumber> = ListView(FXCollections.observableArrayList())
    val coursesPrereqCombo: ComboBox<CourseNumber> = ComboBox(FXCollections.observableArrayList(*(TreasureCourse.COURSENAMES.mapIndexed { i, _ -> CourseNumber(i)}).toTypedArray()))
    
    val groupsRandom: CheckBox = CheckBox()
    val groupsGoalType: ComboBox<GoalType> = ComboBox(FXCollections.observableArrayList(GoalType.Points, GoalType.Lives, GoalType.Monster)).apply {
        selectionModel.select(0)
    }
    val groupsGoalArg: Spinner<Int> = intSpinnerFactory(0, 65535, 0, 1)
    val groupsTempo: Spinner<Int> = intSpinnerFactory(0, 255, 100, 1)

    val gamesComboBox: ComboBox<GameNumber> = ComboBox(FXCollections.observableArrayList(*(0 until 104).map { GameNumber(it.toShort()) }.toTypedArray()))
    val gamesUnknownSpinner: Spinner<Int> = intSpinnerFactory(0, 255, 100, 1)

    init {
        stylesheets += "style/editor.css"
        center = centreVbox

        centreVbox.apply {
            styleClass += "vbox"
        }

        centreVbox.children += HBox().apply {
            styleClass += "Hbox"
            alignment = Pos.CENTER
            children += HBox().apply {
                styleClass += "hbox"
                styleClass += "hbox-centred"
                children += Label("World:")
                children += worldSpinner.apply {
                    styleClass += "long-spinner"
                    selectionModel.select(0)
                    this.selectionModel.selectedIndexProperty().addListener { _ ->
                        onUpdateWorlds()
                    }
                }
            }
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        centreVbox.children += HBox().apply {
            styleClass += "hbox"
            children += VBox().apply {
                styleClass += "vbox"
                HBox.setHgrow(this, Priority.ALWAYS)
                children += Label("Courses")
                children += courses.apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                    this.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                        onUpdateCourses()
                        if (newValue != null) updateCurrentCourse(newValue)
                    }
                }
                children += GridPane().apply {
                    styleClass += "grid-pane"
                    add(Label("Super Hard?:"), 0, 0)
                    add(coursesSuperHard, 1, 0)
                    add(Label("Flow Ball reward:"), 0, 1)
                    add(coursesFlowBalls.apply {
                        styleClass += "short-spinner"
                    }, 1, 1)
                    add(Label("Unknown:"), 0, 2)
                    add(coursesUnknownSpinner.apply {
                        styleClass += "short-spinner"
                    }, 1, 2)
                }
                children += Button("Apply Settings to Course").apply {
                    setOnAction { 
                        
                    }
                }
                children += Separator(Orientation.HORIZONTAL)
                children += Label("Pre-requisite courses:")
                children += coursesPrereqs
                children += Button("Remove Selected Pre-requisite").apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                    disableProperty().bind(Bindings.createBooleanBinding({ coursesPrereqs.items.size <= 0 }, coursesPrereqs.items))
                    setOnAction {
                        val currentCourse = courses.selectionModel.selectedItem
                        if (currentCourse != null) {
                            val selectedPrereq = coursesPrereqs.selectionModel.selectedItem
                            if (selectedPrereq != null && coursesPrereqs.items.size > 0) {
                                coursesPrereqs.items.remove(selectedPrereq)
                                currentCourse.coursesToUnlock.remove(selectedPrereq.id.toShort())
                            }
                        }
                    }
                }
                children += Separator(Orientation.HORIZONTAL)
                children += coursesPrereqCombo.apply {
                    selectionModel.select(0)
                }
                children += Button("Add New Pre-requisite").apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                    setOnAction {
                        val currentCourse = courses.selectionModel.selectedItem
                        if (currentCourse != null) {
                            val selectedPrereq = coursesPrereqCombo.selectionModel.selectedItem
                            if (selectedPrereq != null && selectedPrereq.id.toShort() !in currentCourse.coursesToUnlock) {
                                coursesPrereqs.items.add(selectedPrereq)
                                currentCourse.coursesToUnlock.add(selectedPrereq.id.toShort())
                            }
                        }
                    }
                }
            }
            children += Separator(Orientation.VERTICAL)
            children += VBox().apply {
                styleClass += "vbox"
                HBox.setHgrow(this, Priority.ALWAYS)
                children += Label("Groups")
                children += groups.apply {
                    this.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                        onUpdateGroups()
                        if (newValue != null) updateCurrentGroup(newValue)
                    }
                }
                children += GridPane().apply {
                    styleClass += "grid-pane"
                    add(Label("Random?:"), 0, 0)
                    add(groupsRandom, 1, 0)
                    add(Label("Goal:"), 0, 1)
                    add(HBox().apply {
                        styleClass.addAll("hbox", "hbox-centred")
                        children += groupsGoalType.apply {
                            styleClass += "short-spinner"
                        }
                        children += groupsGoalArg.apply {
                            styleClass += "very-short-spinner"
                        }
                    }, 1, 1)
                    add(Label("Tempo:"), 0, 2)
                    add(HBox().apply {
                        styleClass.addAll("hbox", "hbox-centred")
                        children += groupsTempo.apply {
                            styleClass += "short-spinner"
                        }
                        children += Label("%")
                    }, 1, 2)
                }
                children += Separator(Orientation.HORIZONTAL)
                children += HBox().apply {
                    styleClass += "hbox"
                    styleClass += "hbox-centred"
                    children += Button("Add New").apply {
                        setOnAction {
                            val currentCourse = courses.selectionModel.selectedItem
                            if (currentCourse != null) {
                                val group = TreasureGroup(null)
                                currentCourse.groups.add(group)
                                groups.items.add(group)
                                groups.selectionModel.select(group)
                            }
                        }
                    }
                    children += Button("Apply Settings to Selected").apply {
                        setOnAction {
                            val currentCourse = courses.selectionModel.selectedItem
                            if (currentCourse != null) {
                                val currentGroup = groups.selectionModel.selectedItem
                                if (currentGroup != null) {
                                    currentGroup.random = groupsRandom.isSelected
                                    currentGroup.goal = groupsGoalType.selectionModel.selectedItem ?: GoalType.Points
                                    currentGroup.goalArg = groupsGoalArg.value.toUShort().toShort()
                                    currentGroup.tempo = groupsTempo.value.toUByte().toByte()
                                    groups.refresh()
                                }
                            }
                        }
                    }
                }
                children += HBox().apply {
                    styleClass += "hbox"
                    styleClass += "hbox-centred"
                    children += Button("Remove Selected Group").apply {
                        disableProperty().bind(Bindings.createBooleanBinding({ groups.items.size <= 1 }, groups.items))
                        setOnAction {
                            val currentCourse = courses.selectionModel.selectedItem
                            if (currentCourse != null && groups.items.size > 1) {
                                val currentGroup = groups.selectionModel.selectedItem
                                if (currentGroup != null && groups.items.size > 1) {
                                    currentCourse.groups.remove(currentGroup)
                                    groups.items.remove(currentGroup)
                                    groups.refresh()
                                }
                            }
                        }
                    }
                }
            }
            children += Separator(Orientation.VERTICAL)
            children += VBox().apply {
                styleClass += "vbox"
                HBox.setHgrow(this, Priority.ALWAYS)
                children += Label("Games")
                children += games.apply {
                    this.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                        if (newValue != null) updateCurrentGame(newValue)
                    }
                }
                children += GridPane().apply {
                    styleClass += "grid-pane"
                    add(Label("Game:"), 0, 0)
                    add(gamesComboBox, 1, 0)
                    add(Label("Unknown:"), 0, 1)
                    add(HBox().apply {
                        styleClass.addAll("hbox", "hbox-centred")
                        children += gamesUnknownSpinner.apply {
                            styleClass += "short-spinner"
                        }
                        children += Label("%")
                    }, 1, 1)
                }
                children += Separator(Orientation.HORIZONTAL)
                children += HBox().apply {
                    styleClass += "hbox"
                    styleClass += "hbox-centred"
                    children += Button("Add New").apply {
                        setOnAction {
                            val currentGroup = groups.selectionModel.selectedItem
                            if (currentGroup != null) {
                                val game = TreasureGame(gamesComboBox.selectionModel.selectedItem?.id ?: 0, 100)
                                currentGroup.games.add(game)
                                games.items.add(game)
                                games.selectionModel.select(game)
                                groups.refresh()
                            }
                        }
                    }
                    children += Button("Apply Settings to Selected").apply {
                        setOnAction {
                            val currentGroup = groups.selectionModel.selectedItem
                            if (currentGroup != null) {
                                val selectedGame = games.selectionModel.selectedItem
                                if (selectedGame != null) {
                                    selectedGame.unkPercentage = gamesUnknownSpinner.value.toByte()
                                    selectedGame.id = gamesComboBox.selectionModel.selectedItem?.id ?: 0
                                    games.refresh()
                                }
                            }
                        }
                    }
                }
                children += HBox().apply {
                    styleClass += "hbox"
                    styleClass += "hbox-centred"
                    children += Button("Remove Selected Game").apply {
                        disableProperty().bind(Bindings.createBooleanBinding({ games.items.size <= 1 }, games.items))
                        setOnAction {
                            val currentGroup = groups.selectionModel.selectedItem
                            if (currentGroup != null && games.items.size > 1) {
                                val selectedGame = games.selectionModel.selectedItem
                                if (selectedGame != null) {
                                    currentGroup.games.remove(selectedGame)
                                    games.items.remove(selectedGame)
                                    games.refresh()
                                    groups.refresh()
                                }
                            }
                        }
                    }
                }
            }
        }

        onUpdateWorlds()
    }

    fun onUpdateWorlds() {
        courses.items.clear()
        val worldIndex = worldSpinner.selectionModel.selectedIndex
        courses.items.addAll(treasureData.worlds[worldIndex].courses)
        courses.selectionModel.select(0)
    }

    fun onUpdateCourses() {
        groups.items.clear()
        groups.items.addAll(courses.selectionModel.selectedItem?.groups ?: emptyList())
        groups.selectionModel.select(0)
    }

    fun onUpdateGroups() {
        games.items.clear()
        games.items.addAll(groups.selectionModel.selectedItem?.games ?: emptyList())
        games.selectionModel.select(0)
    }

    fun updateCurrentCourse(course: TreasureCourse) {
        coursesSuperHard.isSelected = course.superHard
        coursesFlowBalls.valueFactory.value = course.flowBalls.toUByte().toInt()
        coursesUnknownSpinner.valueFactory.value = course.unk.toUByte().toInt()
        coursesPrereqs.items.clear()
        course.coursesToUnlock.forEach { id ->
            coursesPrereqs.items.add(CourseNumber(id.toInt()))
        }
    }

    fun updateCurrentGroup(group: TreasureGroup) {
        groupsRandom.isSelected = group.random
        groupsGoalType.selectionModel.select(group.goal)
        groupsGoalArg.valueFactory.value = group.goalArg.toUShort().toInt()
        groupsTempo.valueFactory.value = group.tempo.toUByte().toInt()
    }

    fun updateCurrentGame(game: TreasureGame) {
        gamesComboBox.selectionModel.select(game.id.toInt())
        gamesUnknownSpinner.valueFactory.value = game.unkPercentage.toUByte().toInt()
    }
}