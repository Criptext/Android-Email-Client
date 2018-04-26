package com.email.db

import android.graphics.Color
import com.email.R

/**
 * Created by sebas on 3/15/18.
 */

enum class ColorTypes {
    BLUE,//inbox
    RED,//spam
    GREEN,//sent
    YELLOW,//starred
    GRAY,//draft
    ORANGE,//important
    PURPLE;//trash

    fun toColorResourceId(): Int = when(this) {
        ColorTypes.RED -> R.color.red
        ColorTypes.GREEN -> R.color.green
        ColorTypes.BLUE -> R.color.azure
        ColorTypes.YELLOW -> R.color.yellow
        ColorTypes.GRAY -> R.color.gray
        ColorTypes.ORANGE -> R.color.orange
        ColorTypes.PURPLE -> R.color.purple
    }
}
