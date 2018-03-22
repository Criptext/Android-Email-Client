package com.email.utils.dialog

import android.app.Activity
import android.content.Context
import com.lmntrx.android.library.livin.missme.ProgressDialog

/**
 * Created by gabriel on 7/12/17.
 */

class SingletonProgressDialog(val ctx: Context) {
    private var currentDialog: ProgressDialog? = null


    fun show(message: String) {
        if (currentDialog == null) {
            val pd = ProgressDialog(ctx as Activity)
            pd.setMessage(message)
            pd.show()
            pd.setCancelable(false)
            currentDialog = pd
        }
    }

    fun dismiss() {
        currentDialog?.dismiss()
        currentDialog = null
    }

}
