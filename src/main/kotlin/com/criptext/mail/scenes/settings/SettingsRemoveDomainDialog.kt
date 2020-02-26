package com.criptext.mail.scenes.settings

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.custom_domain.CustomDomainUIObserver
import com.criptext.mail.scenes.settings.devices.DevicesUIObserver
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class SettingsRemoveDomainDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    private lateinit var btnYes: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var view: View

    fun showRemoveDomainDialog(observer: CustomDomainUIObserver?, domainName: String, position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.settings_remove_domain_dialog, null)

        dialogBuilder.setView(view)
        dialogBuilder.setCancelable(false)

        dialog = createDialog(view, dialogBuilder, observer, domainName, position)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: CustomDomainUIObserver?, domainName: String, position: Int): AlertDialog {

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

        assignButtonEvents(dialogView, newRemoveDeviceDialog, observer, domainName, position)


        return newRemoveDeviceDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: CustomDomainUIObserver?, domainName: String, position: Int) {

        btnYes = view.findViewById(R.id.settings_remove_yes) as Button
        btnCancel = view.findViewById(R.id.settings_remove_cancel) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar

        btnYes.setOnClickListener {
            observer?.onRemoveDomainConfirmed(domainName, position)
        }

        btnCancel.setOnClickListener {
            observer?.onRemoveDeviceCancel()
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
