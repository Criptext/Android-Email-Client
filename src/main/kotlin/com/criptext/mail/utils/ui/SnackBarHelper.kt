package com.criptext.mail.utils.ui

import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.utils.getColorFromAttr
import android.widget.TextView
import com.criptext.mail.utils.uiobserver.UIObserver


class SnackBarHelper{
    companion object {
        fun show(view: View, message: String, observer: UIObserver, showAction: Boolean){
            val sb = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            sb.view.setBackgroundColor(view.context.getColorFromAttr(R.attr.criptextSnackBarBackground))
            if(showAction) {
                sb.setAction(R.string.see) {
                    observer.onSnackbarClicked()
                    sb.dismiss()
                }
            }
            val snackbarTextId = R.id.snackbar_text
            val textView = sb.view.findViewById(snackbarTextId) as TextView
            textView.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            sb.show()
        }
    }
}