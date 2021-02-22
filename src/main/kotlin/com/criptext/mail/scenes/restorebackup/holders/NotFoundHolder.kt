package com.criptext.mail.scenes.restorebackup.holders

import android.view.View
import android.widget.TextView
import com.criptext.mail.R

class NotFoundHolder(
        val view: View): BaseRestoreBackupHolder() {

    private val backButton: View = view.findViewById(R.id.icon_back)
    private val skipButton: TextView = view.findViewById(R.id.btn_skip)

    init {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
    }
}