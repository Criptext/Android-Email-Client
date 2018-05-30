package com.email.scenes.labelChooser.data

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

     var isSelected = false

     override fun equals(other: Any?): Boolean {
          other as LabelWrapper
          if (id != other.id) return false
          if (color != other.color) return false
          if (text != other.text) return false

          return true
     }

     override fun hashCode(): Int {
          return id.toInt()
     }
}