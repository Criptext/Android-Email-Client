package com.criptext.mail.scenes.restorebackup.holders

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import com.criptext.mail.R
import com.criptext.mail.scenes.restorebackup.RestoreBackupModel
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.Utility
import com.criptext.mail.utils.getLocalizedUIMessage
import com.google.android.material.textfield.TextInputLayout

class FoundHolder(val view: View, val model: RestoreBackupModel): BaseRestoreBackupHolder() {

    private val textViewTitle: TextView = view.findViewById(R.id.restore_title)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val restoreButton: Button = view.findViewById(R.id.restore_button)
    private val skipButton: TextView = view.findViewById(R.id.btn_skip)
    private val textViewSize: TextView = view.findViewById(R.id.restore_size)
    private val textViewLastModified: TextView = view.findViewById(R.id.restore_last_modified)

    private val password: AppCompatEditText = view.findViewById(R.id.password)
    private val passwordInput: TextInputLayout = view.findViewById(R.id.password_input)

    init {

        textViewSize.visibility = View.GONE
        textViewLastModified.text = if(model.isLocal) view.context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_backed_up, arrayOf(model.lastModified)))
        else view.context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_last_modified, arrayOf(model.lastModified)))

        if(model.isLocal){
            textViewTitle.text = view.context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_local_title))
            skipButton.text = view.context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_dialog_cancel_restore))
        } else {
            textViewTitle.text = view.context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_title))
        }

        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(view.context, R.color.non_criptext_email_send_eye))

        setListeners()
        assignPasswordTextListener()
    }

    fun updateFileData(fileSize: Long, lastModified: Long, isLocal: Boolean) {
        textViewSize.visibility = View.VISIBLE
        textViewSize.text = Utility.humanReadableByteCount(fileSize, true)
        textViewLastModified.text = if(isLocal) view.context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_backed_up, arrayOf(DateAndTimeUtils.getTimeForBackup(lastModified))))
        else view.context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_last_modified, arrayOf(DateAndTimeUtils.getTimeForBackup(lastModified))))
    }

    private fun setListeners(){
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        restoreButton.setOnClickListener {
            uiObserver?.onRestore()
        }
        skipButton.setOnClickListener {
            uiObserver?.onCancelRestore()
        }
    }

    private fun assignPasswordTextListener() {
        password.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onPasswordChangedListener(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }
}