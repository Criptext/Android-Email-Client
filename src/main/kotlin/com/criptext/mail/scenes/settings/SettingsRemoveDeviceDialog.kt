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
import com.criptext.mail.scenes.settings.devices.DevicesUIObserver
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class SettingsRemoveDeviceDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    private lateinit var btnYes: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout
    private lateinit var passwordSuccessImage: ImageView
    private lateinit var passwordErrorImage: ImageView

    private lateinit var view: View

    fun showRemoveDeviceDialog(observer: DevicesUIObserver?, deviceId: Int, position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.settings_remove_device_dialog, null)

        dialogBuilder.setView(view)
        dialogBuilder.setCancelable(false)

        dialog = createDialog(view, dialogBuilder, observer, deviceId, position)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: DevicesUIObserver?, deviceId: Int, position: Int): AlertDialog {

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

        password = dialogView.findViewById(R.id.input) as AppCompatEditText
        passwordInput = dialogView.findViewById(R.id.input_layout)
        passwordSuccessImage = dialogView.findViewById(R.id.success)
        passwordErrorImage = dialogView.findViewById(R.id.error)
        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(context, R.color.non_criptext_email_send_eye))

        assignPasswordTextListener()
        assignButtonEvents(dialogView, newRemoveDeviceDialog, observer, deviceId, position)


        return newRemoveDeviceDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: DevicesUIObserver?, deviceId: Int, position: Int) {

        btnYes = view.findViewById(R.id.settings_remove_yes) as Button
        btnCancel = view.findViewById(R.id.settings_remove_cancel) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar

        btnYes.setOnClickListener {
            observer?.onRemoveDeviceConfirmed(deviceId, position, password.text.toString())
        }

        btnCancel.setOnClickListener {
            observer?.onRemoveDeviceCancel()
            dialog.dismiss()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hidePasswordError() {
        passwordErrorImage.visibility = View.GONE

        passwordInput.error = ""
    }

    @SuppressLint("RestrictedApi")
    private fun showPasswordError(message: UIMessage) {
        passwordErrorImage.visibility = View.VISIBLE

        passwordInput.error = view.context.getLocalizedUIMessage(message)
    }

    fun setPasswordError(message: UIMessage?) {
        if (message == null) {
            hidePasswordError()
            enableSaveButton()
        } else {
            showPasswordError(message)
            disableSaveButton()
        }
    }

    fun disableSaveButton() {
        btnYes.isEnabled = false
    }

    fun enableSaveButton() {
        btnYes.isEnabled = true
    }

    private fun assignPasswordTextListener() {
        password.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, ap2: Int, p3: Int) {
                setPasswordError(null)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
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
