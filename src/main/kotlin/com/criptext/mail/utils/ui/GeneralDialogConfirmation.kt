package com.criptext.mail.utils.ui

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver

class GeneralDialogConfirmation(val context: Context, val data: DialogData.DialogConfirmationData) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    lateinit var btnOk: Button
    lateinit var btnCancel: Button

    private lateinit var view: View

    fun showDialog(observer: UIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.general_confirmation_dialog, null)
        view.findViewById<TextView>(R.id.title).text = context.getLocalizedUIMessage(data.title)
        view.findViewById<TextView>(R.id.message).text = context.getLocalizedUIMessage(data.message.first())

        dialogBuilder.setView(view)

        dialog = createDialog(view, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: UIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window?.setBackgroundDrawable(drawableBackground)
        newLogoutDialog.setCancelable(false)
        newLogoutDialog.setCanceledOnTouchOutside(false)

        assignButtonEvents(dialogView, newLogoutDialog, observer)


        return newLogoutDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: UIObserver?) {

        btnOk = view.findViewById(R.id.btn_ok) as Button
        btnCancel = view.findViewById(R.id.btn_cancel) as Button

        if(!btnOk.hasOnClickListeners()) {
            btnOk.setOnClickListener {
                observer?.onGeneralOkButtonPressed(createResult())
                dialog.dismiss()
            }
        }

        if(!btnCancel.hasOnClickListeners()) {
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    private fun createResult(): DialogResult {
        return when(data.type){
            is DialogType.DeleteAccount,
            is DialogType.ReplyToChange ->
                DialogResult.DialogWithInput("", data.type)
            is DialogType.ManualSyncConfirmation,
            is DialogType.SignIn,
            is DialogType.Message,
            is DialogType.SwitchAccount ->
                DialogResult.DialogConfirmation(data.type)
        }
    }

    fun dismissDialog(){
        dialog?.dismiss()
    }
}
