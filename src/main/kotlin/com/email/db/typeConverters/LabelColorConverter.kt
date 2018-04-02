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
                ColorTypes.BLUE
            } else ->
                ColorTypes.WHITE
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
            else ->
                "white"
        }
    }

}
