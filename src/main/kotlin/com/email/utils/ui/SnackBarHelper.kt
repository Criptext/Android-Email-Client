package com.email.utils.ui

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import com.email.R

class SnackBarHelper{
    companion object {
        fun show(view: View, message: String){
            val sb = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            sb.view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.snackBarColor))
            sb.show()
        }
    }
}