package com.email.utils

import com.email.DB.models.Label

/**
 * Created by sebas on 1/24/18.
 */

class EmailThreadValidator {

   companion object {
      fun isLabelInList (labels: ArrayList<Label>, textLabel: String): Boolean{
         for (label:Label in labels) {
            if(label.text.equals(textLabel)) return true
         }
         return false
      }
   }
}

