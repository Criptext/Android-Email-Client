package com.criptext.mail.scenes.restorebackup.holders

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class ErrorHolder(val view: View, isLocal: Boolean, errorMessage: UIMessage?): BaseRestoreBackupHolder() {

    private val backButton: View = view.findViewById(R.id.icon_back)
    private val retryButton: Button = view.findViewById(R.id.restore_button)
    private val changeAccountButton: Button = view.findViewById(R.id.restore_change_account_button)
    private val skipButton: TextView = view.findViewById(R.id.btn_skip)
    private val subTitle: TextView = view.findViewById(R.id.restore_sub_title)

    init {
        if(errorMessage != null)
            subTitle.text = view.context.getLocalizedUIMessage(errorMessage)
        if(isLocal)
            changeAccountButton.text = view.context
                    .getLocalizedUIMessage(UIMessage(R.string.restore_backup_change_button))
        setListeners()
    }

    fun setListeners(){
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        retryButton.setOnClickListener {
            uiObserver?.onRetryRestore()
        }
        changeAccountButton.setOnClickListener {
            uiObserver?.onChangeDriveAccount()
        }
        skipButton.setOnClickListener {
            uiObserver?.onCancelRestore()
        }
    }
}