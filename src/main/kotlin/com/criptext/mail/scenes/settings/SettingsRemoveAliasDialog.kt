package com.criptext.mail.scenes.settings

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.settings.aliases.AliasesUIObserver
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class SettingsRemoveAliasDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    private lateinit var btnYes: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var view: View

    fun showRemoveAliasDialog(observer: AliasesUIObserver?, aliasAddress: String, domainName: String?, position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.settings_remove_alias_dialog, null)

        dialogBuilder.setView(view)
        dialogBuilder.setCancelable(false)

        dialog = createDialog(view, dialogBuilder, observer, aliasAddress, domainName, position)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: AliasesUIObserver?, aliasAddress: String, domainName: String?,
                             position: Int): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newRemoveDeviceDialog = dialogBuilder.create()
        val window = newRemoveDeviceDialog.window
        newRemoveDeviceDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newRemoveDeviceDialog.window?.setBackgroundDrawable(drawableBackground)
        newRemoveDeviceDialog.setCanceledOnTouchOutside(false)
        newRemoveDeviceDialog.setCancelable(false)
        dialogView.findViewById<TextView>(R.id.message_text).text =
                context.getLocalizedUIMessage(UIMessage(
                        R.string.aliases_delete_dialog_message,
                        arrayOf("$aliasAddress@${domainName ?: Contact.mainDomain}")
                ))

        assignButtonEvents(dialogView, newRemoveDeviceDialog, observer, aliasAddress, domainName, position)


        return newRemoveDeviceDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: AliasesUIObserver?, aliasAddress: String,
                                   domainName: String?, position: Int) {

        btnYes = view.findViewById(R.id.settings_remove_yes) as Button
        btnCancel = view.findViewById(R.id.settings_remove_cancel) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar

        btnYes.setOnClickListener {
            observer?.onRemoveAliasConfirmed(aliasAddress, domainName, position)
        }

        btnCancel.setOnClickListener {
            observer?.onRemoveAliasCancel()
            dialog.dismiss()
        }
    }

    fun disableSaveButton() {
        btnYes.isEnabled = false
    }

    fun enableSaveButton() {
        btnYes.isEnabled = true
    }

    fun toggleLoad(loading: Boolean){
        if(loading){
            progressBar.visibility = View.VISIBLE
            btnYes.visibility = View.GONE
            btnCancel.visibility = View.GONE
        }else{
            progressBar.visibility = View.GONE
            btnYes.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
        }
    }

    fun dismissDialog(){
        dialog?.dismiss()
    }
}
