package rhmodding.treasury

import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import rhmodding.treasury.model.*
import tornadofx.*
import java.io.File

class TreasuryApp : App(TreasuryView::class)

class TreasuryView : View("Treasury v1.1.0") {
	override val root = VBox()
	var path: String = "/"

	lateinit var worldList: ObservableList<String>

	lateinit var courseList: ObservableList<TreasureCourse>
	lateinit var courseListView: ListView<TreasureCourse>
	lateinit var superHardBox: CheckBox
	lateinit var flowBallsSpinner: Spinner<Int>
	lateinit var unkCourseSpinner: Spinner<Int>
	lateinit var unlockList: ObservableList<CourseNumber>
	lateinit var unlockListView: ListView<CourseNumber>

	lateinit var groupList: ObservableList<TreasureGroup>
	lateinit var groupListView: ListView<TreasureGroup>
	lateinit var randomBox: CheckBox
	lateinit var goalBox: ComboBox<GoalType>
	lateinit var goalArgSpinner: Spinner<Int>
	lateinit var tempoSpinner: Spinner<Int>

	lateinit var gameList: ObservableList<TreasureGame>
	lateinit var gameListView: ListView<TreasureGame>
	lateinit var gameBox: ComboBox<GameNumber>
	lateinit var unkSpinner: Spinner<Int>
	lateinit var gameLabel: Label

	var currentWorld: TreasureWorld? = null
	var currentCourse: TreasureCourse? = null
	var currentGroup: TreasureGroup? = null
	var currentGame: TreasureGame? = null
	var treasureData: TreasureData? = null

	fun updateWorld(it: String) {
		currentWorld = treasureData?.worlds?.get(worldList.indexOf(it))
		courseList.removeAll { true }
		courseList.addAll(currentWorld?.courses?: listOf())
		if (courseList.isEmpty()) {
			throw IllegalStateException("Open the directory with the extracted contents of treasure_world_data.zlib first.")
		} else {
			updateCourse(courseList[0])
		}
	}

	fun updateCourse(it: TreasureCourse) {
		currentCourse = it
		groupList.removeAll { true }
		groupList.addAll(currentCourse?.groups?: listOf())
		updateGroup(groupList[0])
		superHardBox.isSelected = it.superHard
		flowBallsSpinner.valueFactory.value = it.flowBalls.toInt() and 0xFF
		unkCourseSpinner.valueFactory.value = it.unk.toInt() and 0xFF
		unlockList.removeAll {true}
		unlockList.addAll(it.coursesToUnlock.map { CourseNumber(it.toInt()) })
	}

	fun updateGroup(it: TreasureGroup) {
		currentGroup = it
		gameList.removeAll { true }
		gameList.addAll(currentGroup?.games?: listOf())
		updateGame(gameList[0])
		randomBox.isSelected = it.random
		goalBox.value = it.goal
		goalArgSpinner.valueFactory.value = it.goalArg.toInt()
		tempoSpinner.valueFactory.value = it.tempo.toInt() and 0xFF
	}

	fun updateGame(it: TreasureGame) {
		currentGame = it
		gameBox.value = GameNumber(it.id)
		unkSpinner.valueFactory.value = it.unkPercentage.toInt() and 0xFF
	}

	init {
		with (root) {
			borderpane {
				top = menubar {
					menu("File") {
						item("Open...") {
							action {
								val dirChooser = DirectoryChooser()
								dirChooser.title = "Choose a directory containing the contents of treasure_world_data.zlib"
								dirChooser.initialDirectory = File(path)

								val f = dirChooser.showDialog(null)
								if (f != null) {
									path = f.parent
									val courses = mutableListOf<TreasureCourse>()
									f.listFiles { _, name -> name.startsWith("world_data_") }.mapTo(courses) { TreasureCourse(it) }
									courses.sortBy { it.id }
									val l = f.listFiles { _, name -> name.startsWith("course_data.bin") }
									if (l.isNotEmpty()) {
										treasureData = TreasureData(l[0], courses)
									}
								}
							}
						}
						item("Save...") {
							action {
								val dirChooser = DirectoryChooser()
								dirChooser.title = "Choose a directory to save your changes in."
								dirChooser.initialDirectory = File(path)

								val f = dirChooser.showDialog(null)
								if (f != null) {
									path = f.parent
									val c_d = File(f, "course_data.bin")
									c_d.writeBytes(treasureData!!.toBytes().toByteArray())
									for (w in treasureData!!.worlds) {
										for (c in w.courses) {
											val w_d = File(f, "world_data_${c.id}.bin")
											w_d.writeBytes(c.toBytes().toByteArray())
										}
									}
								}
							}
						}
					}
				}
				bottom = hbox(spacing = 6) {
					paddingAll = 10
					vbox(spacing = 5) {
						label("Worlds")
						worldList = observableArrayList("0 - Saffron World", "1 - Saltwater World", "2 - Paprika World")
						listview(worldList) {
							onUserSelect(1) {
								updateWorld(it)
							}
						}
					}
					vbox(spacing = 5) {
						maxWidth = prefWidth
						label("Courses")
						courseList = observableArrayList<TreasureCourse>()
						courseListView = listview(courseList) {
							onUserSelect(1) {
								updateCourse(it)
							}
						}
						superHardBox = checkbox("Super Hard?") {
							selectedProperty().addListener { _, _, newValue ->
								currentCourse?.superHard = newValue
							}
						}
						hbox(spacing = 0) {
							label("Flow Ball Reward: ")
							flowBallsSpinner = spinner(0, 255, 1, 1, true) {
								prefWidth = 70.0
								valueProperty().addListener { _, _, newValue ->
									currentCourse?.flowBalls = newValue.toByte()
								}
							}
						}
						hbox(spacing = 0) {
							label("Unknown Value: ")
							unkCourseSpinner = spinner(0, 255, 3, 1, true) {
								prefWidth = 70.0
								valueProperty().addListener { _, _, newValue ->
									currentCourse?.unk = newValue.toByte()
								}
							}
						}
						label("Courses Needed to Unlock:")
						unlockList = observableArrayList()
						unlockListView = listview(unlockList) {
							prefHeight = 100.0
						}
						hbox(spacing = 0) {
							button("Add") {
								action {
									unlockList.add(CourseNumber(0))
									currentCourse?.coursesToUnlock?.add(0)
								}
							}
							button("Remove") {
								action {
									val item = unlockListView.selectedItem
									unlockList.remove(item)
									currentCourse?.coursesToUnlock?.remove(item?.id?.toShort()?:-1)
								}
							}
						}
						var lbl: Label? = null
						hbox(spacing = 5) {
							spinner(0, 39, 0, 1, true) {
								prefWidth = 70.0
								valueProperty().addListener { _, _, newValue ->
									lbl?.text = TreasureCourse.COURSENAMES[newValue]
									unlockListView.selectedItem?.id = newValue
									unlockListView.refresh()
									currentCourse?.coursesToUnlock?.removeAll{true}
									currentCourse?.coursesToUnlock?.addAll(unlockList.map { it.id.toShort() })
								}
							}
							lbl = label("")
						}
					}
					vbox(spacing = 5) {
						label("Groups")
						groupList = observableArrayList<TreasureGroup>()
						groupListView = listview(groupList) {
							onUserSelect(1) {
								updateGroup(it)
							}
						}
						hbox(spacing = 10) {
							button("Add") {
								action {
									val group = TreasureGroup(null)
									currentCourse?.groups?.add(group)
									groupList.add(group)
								}
							}
							button("Remove") {
								if (groupList.size > 1) {
									groupList.remove(currentGroup)
									currentCourse?.groups?.remove(currentGroup)
								}
							}
						}
						randomBox = checkbox("Random?") {
							selectedProperty().addListener { _, _, newValue ->
								currentGroup?.random = newValue
								groupListView.refresh()
							}
						}
						hbox(spacing = 0) {
							alignment = Pos.CENTER_LEFT
							label("Goal: ")
							goalBox = combobox(null, listOf(GoalType.Points, GoalType.Lives, GoalType.Monster)) {
								valueProperty().addListener { _, _, newValue ->
									currentGroup?.goal = newValue
									groupListView.refresh()
								}
							}
							label(" ")
							goalArgSpinner = spinner(0, 65535, 0, 1, true) {
								prefWidth = 70.0
								valueProperty().addListener { _, _, newValue ->
									currentGroup?.goalArg = newValue.toShort()
									groupListView.refresh()
								}
							}
						}
						hbox(spacing = 0) {
							alignment = Pos.CENTER_LEFT
							label("Tempo: ")
							tempoSpinner = spinner(1, 255, 100, 1, true) {
								prefWidth = 70.0
								valueProperty().addListener { _, _, newValue ->
									currentGroup?.tempo = newValue.toByte()
									groupListView.refresh()
								}
							}
							label("%")
						}
					}
					vbox(spacing = 5) {
						label("Games")
						gameList = observableArrayList<TreasureGame>()
						gameListView = listview(gameList) {
							onUserSelect(1) {
								updateGame(it)
							}
						}
						hbox(spacing = 10) {
							button("Add") {
								action {
									val game = TreasureGame(0, 100)
									currentGroup?.games?.add(game)
									gameList.add(game)
									groupListView.refresh()
								}
							}
							button("Remove") {
								action {
									if (gameList.size > 1) {
										gameList.remove(currentGame)
										currentGroup?.games?.remove(currentGame)
										groupListView.refresh()
									}
								}
							}
						}
						hbox(spacing = 0) {
							alignment = Pos.CENTER_LEFT
							label("Game: ")
							gameBox = combobox {
								for (i in 0 until 104) {
									items.add(GameNumber(i.toShort()))
								}
								valueProperty().addListener { _, _, newValue ->
									currentGame?.id = newValue.id
									gameListView.refresh()
									courseListView.refresh()
								}
							}
						}
						hbox(spacing = 0) {
							alignment = Pos.CENTER_LEFT
							label("Unknown: ")
							unkSpinner = spinner(0, 255, 100, 1, true) {
								prefWidth = 70.0
								valueProperty().addListener { _, _, newValue ->
									currentGame?.unkPercentage = newValue.toByte()
									gameListView.refresh()
									groupListView.refresh()
								}
							}
							label("%")
						}
					}
				}
			}
		}
	}
}