package com.criptext.mail.utils.ui

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.utils.getColorFromAttr
import android.widget.TextView



class SnackBarHelper{
    companion object {
        fun show(view: View, message: String){
            val sb = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            sb.view.setBackgroundColor(view.context.getColorFromAttr(R.attr.criptextSnackBarBackground))
            val snackbarTextId = android.support.design.R.id.snackbar_text
            val textView = sb.view.findViewById(snackbarTextId) as TextView
            textView.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            sb.show()
        }
    }
}