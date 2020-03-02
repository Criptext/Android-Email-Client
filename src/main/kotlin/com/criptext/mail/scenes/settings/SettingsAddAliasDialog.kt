package com.criptext.mail.scenes.settings

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.settings.aliases.AliasesUIObserver
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.google.android.material.textfield.TextInputLayout


class SettingsAddAliasDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    private lateinit var btnYes: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout

    private lateinit var view: View

    private var aliasDomain = "@${Contact.mainDomain}"

    fun showAddAliasDialog(observer: AliasesUIObserver?, domains: List<String>) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.settings_add_alias_dialog, null)

        dialogBuilder.setView(view)
        dialogBuilder.setCancelable(false)

        dialog = createDialog(view, dialogBuilder, observer, domains)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: AliasesUIObserver?, domains: List<String>): AlertDialog {

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

        password.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, ap2: Int, p3: Int) {
                observer?.onAddAliasTextChanged(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        if(domains.isNotEmpty()){
            dialogView.findViewById<TextView>(R.id.criptext_domain_text).visibility = View.GONE
            val spinner = dialogView.findViewById<Spinner>(R.id.domain_spiner)
            spinner.visibility = View.VISIBLE
            val dataAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, domains)
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = dataAdapter
            spinner.setSelection(0)
            spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val item = p0?.getItemAtPosition(p2)?.toString() ?: return
                    aliasDomain = item
                    observer?.onAddAliasSpinnerChangeSelection(aliasDomain)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

        assignButtonEvents(dialogView, newRemoveDeviceDialog, observer)


        return newRemoveDeviceDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: AliasesUIObserver?) {

        btnYes = view.findViewById(R.id.settings_remove_yes) as Button
        btnCancel = view.findViewById(R.id.settings_remove_cancel) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar

        btnYes.setOnClickListener {
            observer?.onAddAliasOkPressed(password.text.toString(), aliasDomain)
        }

        btnCancel.setOnClickListener {
            observer?.onRemoveAliasCancel()
            dialog.dismiss()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hidePasswordError() {
        passwordInput.error = ""
    }

    @SuppressLint("RestrictedApi")
    private fun showPasswordError(message: UIMessage) {
        passwordInput.error = view.context.getLocalizedUIMessage(message)
    }

    fun setError(message: UIMessage?) {
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
