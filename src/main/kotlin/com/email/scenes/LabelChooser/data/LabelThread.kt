package com.email.scenes.LabelChooser.data

import com.email.db.models.Label

/**
 * Created by sebas on 2/2/18.
 */

class LabelThread(val label: Label) {
     val color : String
          get() = label.color
     val text : String
          get() = label.text
     val id : Int
          get() = label.id!!

     var isSelected = false
}