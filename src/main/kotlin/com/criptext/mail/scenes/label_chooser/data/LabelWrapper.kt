package com.criptext.mail.scenes.label_chooser.data

import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.models.Label

/**
 * Created by sebas on 2/2/18.
 */

data class LabelWrapper(val label: Label) {

    val color : String
        get() = label.color
    var text : String
        get() = label.text
        set(value) {label.text = value}
    val id : Long
        get() = label.id
    val type : LabelTypes
        get() = label.type
    val visible: Boolean
        get() = label.visible
    val uuid: String
        get() = label.uuid

    var isSelected = false
}