package com.email.scenes.label_chooser.data

import com.email.db.LabelTypes
import com.email.db.models.Label
import com.email.db.typeConverters.LabelTextConverter

/**
 * Created by sebas on 2/2/18.
 */

data class LabelWrapper(val label: Label) {

    val color : String
        get() = label.color
    val text : String
        get() = LabelTextConverter().parseLabelTextType(label.text)
    val id : Long
        get() = label.id
    val type : LabelTypes
        get() = label.type

    var isSelected = false
}