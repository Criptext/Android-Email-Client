package com.email.db.typeConverters

import android.arch.persistence.room.TypeConverter
import com.email.db.ColorTypes

/**
 * Created by sebas on 3/15/18.
 */

class LabelColorConverter {

    @TypeConverter
    fun getColorType(value: String) : ColorTypes  {
        return when(value) {
            "green", "GREEN" -> {
                ColorTypes.GREEN
            }
            "red", "RED" -> {
                ColorTypes.RED
            }
            "blue", "BLUE" -> {
                ColorTypes.BLUE
            }
            "yellow", "YELLOW" -> {
                ColorTypes.YELLOW
            }
            "orange", "ORANGE" -> {
                ColorTypes.ORANGE
            }
            "purple", "PURPLE" -> {
                ColorTypes.PURPLE
            }
            else -> {
                ColorTypes.GRAY
            }
        }
    }

    @TypeConverter
    fun parseColorType(value: ColorTypes): String {
        return when(value) {
            ColorTypes.GREEN -> {
                "green"
            }
            ColorTypes.RED -> {
                "red"
            }
            ColorTypes.BLUE -> {
                "blue"
            }
            ColorTypes.YELLOW -> {
                "yellow"
            }
            ColorTypes.ORANGE -> {
                "orange"
            }
            ColorTypes.PURPLE -> {
                "purple"
            }
            else ->
                "gray"
        }
    }

}
