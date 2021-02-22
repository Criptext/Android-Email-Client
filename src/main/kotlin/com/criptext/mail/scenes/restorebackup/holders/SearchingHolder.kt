package com.criptext.mail.scenes.restorebackup.holders

import android.view.View
import com.criptext.mail.R

class SearchingHolder(
        val view: View): BaseRestoreBackupHolder() {

    private val backButton: View = view.findViewById(R.id.icon_back)

    init {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
    }
}