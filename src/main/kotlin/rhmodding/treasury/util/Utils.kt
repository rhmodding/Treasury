package rhmodding.treasury.util

import javafx.event.EventHandler
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.util.StringConverter


fun Stage.setMinimumBoundsToSized() {
    this.sizeToScene()
    this.minWidth = this.width
    this.minHeight = this.height
}

val Double.em: Double get() = Font.getDefault().size * this

fun doubleSpinnerFactory(min: Double, max: Double, initial: Double, step: Double = 1.0): Spinner<Double> =
    Spinner<Double>().apply {
        valueFactory = SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step).apply factory@{
            this.converter = object : StringConverter<Double>() {
                override fun toString(`object`: Double): String {
                    return `object`.toString()
                }

                override fun fromString(string: String): Double {
                    return string.toDoubleOrNull()?.coerceIn(this@factory.min, this@factory.max) ?: 0.0
                }
            }
        }
        isEditable = true
    }

fun intSpinnerFactory(min: Int, max: Int, initial: Int, step: Int = 1): Spinner<Int> =
    Spinner<Int>().apply {
        valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial, step).apply factory@{
            this.converter = object : StringConverter<Int>() {
                override fun toString(`object`: Int): String {
                    return `object`.toString()
                }

                override fun fromString(string: String): Int {
                    return string.toIntOrNull()?.coerceIn(this@factory.min, this@factory.max) ?: 0
                }
            }
        }
        isEditable = true
    }

fun <T> Spinner<T>.spinnerArrowKeys(): Spinner<T> {
    if (this.isEditable) {
        this.editor?.onKeyPressed = EventHandler { evt ->
            when(evt.code) {
                KeyCode.UP -> this.increment(if (evt.isShortcutDown && !evt.isShiftDown && !evt.isAltDown) 10 else 1)
                KeyCode.DOWN -> this.decrement(if (evt.isShortcutDown && !evt.isShiftDown && !evt.isAltDown) 10 else 1)
                else -> {}
            }
        }
    }
    return this
}

/**
 * @return true iff there is a selection and the selection is contiguous
 */
fun <T> MultipleSelectionModel<T>.isSelectionContiguous(): Boolean {
    if (this.selectedIndices.isEmpty()) return false
    if (this.selectedIndices.size == 1) return true
    val sorted = this.selectedIndices.toList().sorted()
    return sorted.first() + sorted.size - 1 == sorted.last()
}