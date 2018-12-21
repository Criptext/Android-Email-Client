package com.criptext.mail.utils.ui

import android.support.design.widget.Snackbar
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.utils.getColorFromAttr

class SnackBarHelper{
    companion object {
        fun show(view: View, message: String){
            val sb = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            sb.view.setBackgroundColor(view.context.getColorFromAttr(R.attr.criptextSnackBarBackground))
            sb.show()
        }
    }
}