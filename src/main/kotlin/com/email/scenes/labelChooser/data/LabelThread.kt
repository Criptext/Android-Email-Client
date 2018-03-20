package com.email.scenes.labelChooser.data

import com.email.db.ColorTypes
import com.email.db.models.Label

/**
 * Created by sebas on 2/2/18.
 */

class LabelThread(val label: Label) {
     val color : ColorTypes
          get() = label.color
     val text : String
          get() = label.text
     val id : Int
          get() = label.id!!

     var isSelected = false

     override fun equals(other: Any?): Boolean {

          other as Label
          if (id != other.id) return false
          if (color != other.color) return false
          if (text != other.text) return false

          return true
     }

     override fun hashCode(): Int {
          return id
     }
}